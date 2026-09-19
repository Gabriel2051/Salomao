/* SALOMÃO — mapa mental interativo: criar/mover/editar/excluir nós,
   conectar/desconectar, zoom/pan, centralizar, autosave no banco. */
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    var svg = document.getElementById("mapaCanvas");
    if (!svg) return;
    var NS = "http://www.w3.org/2000/svg";
    var mapId = svg.getAttribute("data-mapa-id");
    var grafoUrl = "/mapas/" + mapId + "/grafo";
    var viewport = document.getElementById("mapaViewport");
    var camadaArestas = document.getElementById("mapaArestas");
    var camadaNos = document.getElementById("mapaNos");

    var nos = [];
    var arestas = [];
    var pan = { x: 40, y: 40, z: 1 };
    var selecionado = null;
    var origemLigacao = null;
    var arrasto = null;

    function uid() {
      return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0;
        return (c === "x" ? r : (r & 0x3 | 0x8)).toString(16);
      });
    }
    function esc(s) { return window.salomaoEsc ? window.salomaoEsc(s) : String(s); }

    function aplicarTransform() {
      viewport.setAttribute("transform", "translate(" + pan.x + "," + pan.y + ") scale(" + pan.z + ")");
    }

    function telaParaMundo(evt) {
      var pt = svg.createSVGPoint();
      pt.x = evt.clientX; pt.y = evt.clientY;
      var m = viewport.getCTM();
      if (!m) return { x: 0, y: 0 };
      var p = pt.matrixTransform(m.inverse());
      return { x: p.x, y: p.y };
    }

    function noPorId(id) { return nos.find(function (n) { return n.id === id; }); }

    function desenhar() {
      camadaArestas.innerHTML = "";
      camadaNos.innerHTML = "";
      arestas.forEach(function (a) {
        var s = noPorId(a.sourceId), t = noPorId(a.targetId);
        if (!s || !t) return;
        var l = document.createElementNS(NS, "line");
        l.setAttribute("x1", s.x); l.setAttribute("y1", s.y);
        l.setAttribute("x2", t.x); l.setAttribute("y2", t.y);
        l.setAttribute("class", "aresta");
        l.style.cursor = "pointer";
        l.addEventListener("click", function (e) {
          e.stopPropagation();
          if (window.confirm("Remover esta conexão?")) {
            arestas = arestas.filter(function (x) { return x !== a; });
            desenhar(); notificar();
          }
        });
        camadaArestas.appendChild(l);
        if (a.label) {
          var tx = document.createElementNS(NS, "text");
          tx.setAttribute("x", (s.x + t.x) / 2); tx.setAttribute("y", (s.y + t.y) / 2 - 6);
          tx.setAttribute("class", "aresta-label");
          tx.setAttribute("text-anchor", "middle");
          tx.textContent = a.label;
          camadaArestas.appendChild(tx);
        }
      });
      nos.forEach(function (n) {
        var g = document.createElementNS(NS, "g");
        g.setAttribute("class", "no" + (selecionado === n.id ? " selecionado" : "") + (origemLigacao === n.id ? " selecionado" : ""));
        g.setAttribute("transform", "translate(" + n.x + "," + n.y + ")");
        var w = Math.max(120, n.label.length * 7.5 + 28);
        var r = document.createElementNS(NS, "rect");
        r.setAttribute("x", -w / 2); r.setAttribute("y", -24);
        r.setAttribute("width", w); r.setAttribute("height", 48);
        r.setAttribute("rx", 8);
        if (n.color) r.style.stroke = n.color;
        g.appendChild(r);
        var t1 = document.createElementNS(NS, "text");
        t1.setAttribute("text-anchor", "middle"); t1.setAttribute("y", -4);
        t1.textContent = n.label.length > 26 ? n.label.slice(0, 25) + "…" : n.label;
        g.appendChild(t1);
        var t2 = document.createElementNS(NS, "text");
        t2.setAttribute("text-anchor", "middle"); t2.setAttribute("y", 13);
        t2.setAttribute("class", "tipo");
        t2.textContent = (n.nodeType || "TEXTO") + (n.refType ? " · " + n.refType : "");
        g.appendChild(t2);
        g.addEventListener("mousedown", function (e) {
          e.stopPropagation();
          if (origemLigacao && origemLigacao !== n.id) {
            arestas.push({ id: uid(), sourceId: origemLigacao, targetId: n.id, label: "" });
            origemLigacao = null;
            desenhar(); notificar(); atualizarPainel();
            return;
          }
          arrasto = { id: n.id, dx: 0, dy: 0 };
        });
        g.addEventListener("click", function (e) {
          e.stopPropagation();
          selecionado = n.id;
          desenhar(); atualizarPainel();
        });
        g.addEventListener("dblclick", function (e) { e.stopPropagation(); selecionarParaEdicao(n); });
        camadaNos.appendChild(g);
      });
      aplicarTransform();
    }

    function notificar() {
      var root = document.querySelector("[data-autosave]");
      if (root) root.dispatchEvent(new Event("input", { bubbles: true }));
      // dispara autosave diretamente
      var ev = new CustomEvent("editor:change", { bubbles: true });
      document.querySelector(".mapa-wrap").dispatchEvent(ev);
    }

    // pan do canvas + zoom
    svg.addEventListener("mousedown", function (e) {
      arrasto = arrasto || { pan: true, sx: e.clientX, sy: e.clientY, px: pan.x, py: pan.y };
    });
    window.addEventListener("mousemove", function (e) {
      if (!arrasto) return;
      if (arrasto.pan) {
        var rect = svg.getBoundingClientRect();
        var scale = rect.width / svg.viewBox.baseVal.width || 1;
        pan.x = arrasto.px + (e.clientX - arrasto.sx) / scale;
        pan.y = arrasto.py + (e.clientY - arrasto.sy) / scale;
        aplicarTransform();
      } else if (arrasto.id) {
        var n = noPorId(arrasto.id);
        if (n) {
          var p = telaParaMundo(e);
          n.x = Math.round(p.x); n.y = Math.round(p.y);
          desenhar();
        }
      }
    });
    window.addEventListener("mouseup", function () {
      if (arrasto && arrasto.id) notificar();
      arrasto = null;
    });
    svg.addEventListener("wheel", function (e) {
      e.preventDefault();
      pan.z = Math.min(2.5, Math.max(0.3, pan.z + (e.deltaY < 0 ? 0.1 : -0.1)));
      aplicarTransform();
    }, { passive: false });
    svg.addEventListener("dblclick", function (e) {
      var p = telaParaMundo(e);
      var label = window.prompt("Texto do novo nó:", "Nova ideia");
      if (!label) return;
      nos.push({ id: uid(), nodeType: "TEXTO", label: label, content: "", refType: null, refId: null, x: Math.round(p.x), y: Math.round(p.y), color: "", collapsed: false });
      desenhar(); notificar();
    });

    // painel lateral
    var painel = document.getElementById("mapaPainel");
    function atualizarPainel() {
      if (!painel) return;
      var n = noPorId(selecionado);
      if (!n) {
        painel.innerHTML = "<p class='muted'>Selecione um nó para editar. Duplo-clique no fundo cria um nó. Arraste para mover. Use “Ligar” e clique em outro nó para conectar.</p>";
        return;
      }
      painel.innerHTML =
        "<div class='campo'><label>Rótulo</label><input id='mpLabel' type='text' value='" + esc(n.label) + "'></div>" +
        "<div class='campo'><label>Tipo</label><select id='mpTipo'>" +
        ["TEXTO", "PERSONAGEM", "PODER", "HISTORIA", "ANOTACAO", "CATEGORIA", "LIVRE"].map(function (t) {
          return "<option" + (n.nodeType === t ? " selected" : "") + ">" + t + "</option>";
        }).join("") + "</select></div>" +
        "<div class='campo'><label>Conteúdo / notas</label><textarea id='mpConteudo' rows='3'>" + esc(n.content || "") + "</textarea></div>" +
        "<div class='campo'><label>Cor (ex.: #B5121B)</label><input id='mpCor' type='text' value='" + esc(n.color || "") + "'></div>" +
        "<div style='display:flex;gap:8px;flex-wrap:wrap'>" +
        "<button class='btn pequeno' id='mpLigar'>Ligar a outro nó</button>" +
        "<button class='btn pequeno perigo' id='mpExcluir'>Excluir nó</button></div>";
      document.getElementById("mpLabel").addEventListener("input", function (e) { n.label = e.target.value; desenhar(); });
      document.getElementById("mpLabel").addEventListener("change", notificar);
      document.getElementById("mpTipo").addEventListener("change", function (e) { n.nodeType = e.target.value; desenhar(); notificar(); });
      document.getElementById("mpConteudo").addEventListener("change", function (e) { n.content = e.target.value; notificar(); });
      document.getElementById("mpCor").addEventListener("change", function (e) { n.color = e.target.value; desenhar(); notificar(); });
      document.getElementById("mpLigar").addEventListener("click", function () {
        origemLigacao = n.id;
        painel.innerHTML += "";
        atualizarPainel();
        alert("Modo ligação: clique em outro nó para conectar.");
      });
      document.getElementById("mpExcluir").addEventListener("click", function () {
        if (!window.confirm("Excluir este nó e suas conexões?")) return;
        nos = nos.filter(function (x) { return x.id !== n.id; });
        arestas = arestas.filter(function (a) { return a.sourceId !== n.id && a.targetId !== n.id; });
        selecionado = null;
        desenhar(); atualizarPainel(); notificar();
      });
    }
    function selecionarParaEdicao(n) { selecionado = n.id; desenhar(); atualizarPainel(); }

    // barra de ferramentas
    var btnCentralizar = document.getElementById("mapaCentralizar");
    if (btnCentralizar) btnCentralizar.addEventListener("click", function () {
      if (!nos.length) { pan = { x: 40, y: 40, z: 1 }; aplicarTransform(); return; }
      var sx = nos.reduce(function (a, n) { return a + n.x; }, 0) / nos.length;
      var sy = nos.reduce(function (a, n) { return a + n.y; }, 0) / nos.length;
      pan.x = 500 - sx * pan.z; pan.y = 280 - sy * pan.z;
      aplicarTransform();
    });
    var btnZoomMais = document.getElementById("mapaZoomMais");
    if (btnZoomMais) btnZoomMais.addEventListener("click", function () { pan.z = Math.min(2.5, pan.z + 0.15); aplicarTransform(); });
    var btnZoomMenos = document.getElementById("mapaZoomMenos");
    if (btnZoomMenos) btnZoomMenos.addEventListener("click", function () { pan.z = Math.max(0.3, pan.z - 0.15); aplicarTransform(); });
    var btnNovoNo = document.getElementById("mapaNovoNo");
    if (btnNovoNo) btnNovoNo.addEventListener("click", function () {
      nos.push({ id: uid(), nodeType: "TEXTO", label: "Nova ideia", content: "", refType: null, refId: null, x: Math.round(500 - pan.x + Math.random() * 120 - 60), y: Math.round(280 - pan.y + Math.random() * 120 - 60), color: "", collapsed: false });
      desenhar(); notificar();
    });

    // coleta para o autosave
    window.salomaoMapaColetar = function () {
      return {
        nos: nos.map(function (n) {
          return { id: /-/.test(n.id) && n.id.length > 20 ? n.id : null, nodeType: n.nodeType, label: n.label, content: n.content || "", refType: n.refType, refId: n.refId, x: n.x, y: n.y, color: n.color || "", collapsed: !!n.collapsed };
        }),
        arestas: arestas.map(function (a) { return { id: null, sourceId: a.sourceId, targetId: a.targetId, label: a.label || "" }; })
      };
    };

    // carrega
    window.salomaoFetch(grafoUrl).then(function (g) {
      nos = (g.nos || []).map(function (n) {
        return { id: n.id, nodeType: n.nodeType, label: n.label, content: n.content || "", refType: n.refType, refId: n.refId, x: n.x, y: n.y, color: n.color || "", collapsed: !!n.collapsed };
      });
      arestas = (g.arestas || []).map(function (a) {
        return { id: a.id, sourceId: a.sourceId, targetId: a.targetId, label: a.label || "" };
      });
      // reatribui ids locais estáveis (backend gera novos ids a cada save; mantemos por posição)
      desenhar(); atualizarPainel();
      if (btnCentralizar) btnCentralizar.click();
    }).catch(function () {
      if (painel) painel.innerHTML = "<p class='muted'>Não foi possível carregar o mapa.</p>";
    });
    atualizarPainel();
  });
})();

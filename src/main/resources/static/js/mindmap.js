/* SALOMÃO — mapa mental v2: nós com tipo/cor/ícone, vínculo real a entidades
   (personagem, poder, história, anotação, item do catálogo), arestas curvas com
   rótulo e seta, recolher ramos, auto-layout, zoom/pan/fit, atalhos de teclado
   e autosave no banco. Contrato de persistência: window.salomaoMapaColetar(). */
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
    var REFS = window.MAPA_REFS || { characters: [], powers: [], stories: [], notes: [], items: [] };

    var TIPOS = {
      TEXTO:      { nome: "Texto",      cor: "#8a8a8a", glifo: "¶" },
      PERSONAGEM: { nome: "Personagem", cor: "#c0392b", glifo: "♟", refs: "characters", rotulo: "Personagem vinculado" },
      PODER:      { nome: "Poder",      cor: "#8e44ad", glifo: "✦", refs: "powers",     rotulo: "Poder vinculado" },
      HISTORIA:   { nome: "História",   cor: "#b08a3e", glifo: "❧", refs: "stories",    rotulo: "História vinculada" },
      ANOTACAO:   { nome: "Anotação",   cor: "#4a7fb5", glifo: "✎", refs: "notes",      rotulo: "Anotação vinculada" },
      CATEGORIA:  { nome: "Categoria",  cor: "#5a5a5a", glifo: "▤" },
      ITEM:       { nome: "Item",       cor: "#3fa46a", glifo: "❖", refs: "items",      rotulo: "Item do catálogo" },
      LIVRE:      { nome: "Livre",      cor: "#6a6a6a", glifo: "○" }
    };
    var CORES = ["", "#B5121B", "#8e44ad", "#b08a3e", "#4a7fb5", "#3fa46a", "#c9a227", "#5a5a5a"];
    var URLS = { CHARACTER: "/personagens/", POWER: "/poderes/", STORY: "/historias/", NOTE: "/anotacoes/", ITEM: "/catalogo/" };
    var REF_POR_TIPO = { PERSONAGEM: "CHARACTER", PODER: "POWER", HISTORIA: "STORY", ANOTACAO: "NOTE", ITEM: "ITEM" };

    var nos = [];
    var arestas = [];
    var pan = { x: 40, y: 40, z: 1 };
    var selecionado = null;      // id de nó selecionado
    var arestaSelecionada = null; // id de aresta selecionada
    var origemLigacao = null;
    var arrasto = null;
    var ultimoArrasto = 0;       // timestamp: evita "clique" logo após arrastar

    function uid() {
      return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0;
        return (c === "x" ? r : (r & 0x3 | 0x8)).toString(16);
      });
    }
    function esc(s) { return window.salomaoEsc ? window.salomaoEsc(s) : String(s); }
    function noPorId(id) { return nos.find(function (n) { return n.id === id; }); }
    function arestaPorId(id) { return arestas.find(function (a) { return a.id === id; }); }
    function corDe(n) { return n.color || (TIPOS[n.nodeType] ? TIPOS[n.nodeType].cor : "#8a8a8a"); }
    function glifoDe(n) { return TIPOS[n.nodeType] ? TIPOS[n.nodeType].glifo : "¶"; }

    // ---------- geometria ----------
    function aplicarTransform() {
      viewport.setAttribute("transform", "translate(" + pan.x + "," + pan.y + ") scale(" + pan.z + ")");
      var z = document.getElementById("mapaZoomNivel");
      if (z) z.textContent = Math.round(pan.z * 100) + "%";
    }
    function telaParaMundo(evt) {
      var pt = svg.createSVGPoint();
      pt.x = evt.clientX; pt.y = evt.clientY;
      var m = viewport.getCTM();
      if (!m) return { x: 0, y: 0 };
      var p = pt.matrixTransform(m.inverse());
      return { x: p.x, y: p.y };
    }
    function larguraDe(n) { return Math.max(132, n.label.length * 7.6 + 44); }
    function ocultos() {
      // nós escondidos por um ancestral recolhido (propaga pelas arestas)
      var escondidos = {};
      var fechados = {};
      nos.forEach(function (n) { if (n.collapsed) fechados[n.id] = true; });
      var mudou = true;
      while (mudou) {
        mudou = false;
        arestas.forEach(function (a) {
          if ((fechados[a.sourceId] || escondidos[a.sourceId]) && !escondidos[a.targetId]) {
            escondidos[a.targetId] = true; mudou = true;
          }
        });
      }
      return escondidos;
    }

    // ---------- desenho ----------
    function desenhar() {
      camadaArestas.innerHTML = "";
      camadaNos.innerHTML = "";
      var escondidos = ocultos();

      arestas.forEach(function (a) {
        var s = noPorId(a.sourceId), t = noPorId(a.targetId);
        if (!s || !t || escondidos[a.sourceId] || escondidos[a.targetId]) return;
        var dx = t.x - s.x, dy = t.y - s.y;
        var curv = Math.max(40, Math.abs(dx) * 0.4);
        var path = document.createElementNS(NS, "path");
        path.setAttribute("d", "M " + s.x + " " + s.y +
          " C " + (s.x + curv) + " " + s.y + ", " + (t.x - curv) + " " + t.y + ", " + t.x + " " + t.y);
        path.setAttribute("class", "aresta" + (arestaSelecionada === a.id ? " selecionada" : ""));
        path.setAttribute("marker-end", "url(#mapaSeta)");
        path.addEventListener("click", function (e) {
          e.stopPropagation();
          arestaSelecionada = a.id; selecionado = null;
          desenhar(); atualizarPainel();
        });
        camadaArestas.appendChild(path);
        // hitbox invisível mais larga: clicar na curva não exige precisão
        var hit = document.createElementNS(NS, "path");
        hit.setAttribute("d", path.getAttribute("d"));
        hit.setAttribute("class", "aresta-hit");
        hit.addEventListener("click", function (e) {
          e.stopPropagation();
          arestaSelecionada = a.id; selecionado = null;
          desenhar(); atualizarPainel();
        });
        camadaArestas.appendChild(hit);
        if (a.label) {
          var mx = (s.x + t.x) / 2, my = (s.y + t.y) / 2 - Math.abs(dy) * 0.12 - 8;
          var tx = document.createElementNS(NS, "text");
          tx.setAttribute("x", mx); tx.setAttribute("y", my);
          tx.setAttribute("class", "aresta-label");
          tx.setAttribute("text-anchor", "middle");
          tx.textContent = a.label;
          camadaArestas.appendChild(tx);
        }
      });

      nos.forEach(function (n) {
        if (escondidos[n.id]) return;
        var g = document.createElementNS(NS, "g");
        var cls = "no";
        if (selecionado === n.id) cls += " selecionado";
        if (origemLigacao === n.id) cls += " ligando";
        g.setAttribute("class", cls);
        g.setAttribute("transform", "translate(" + n.x + "," + n.y + ")");
        var w = larguraDe(n);
        var r = document.createElementNS(NS, "rect");
        r.setAttribute("x", -w / 2); r.setAttribute("y", -24);
        r.setAttribute("width", w); r.setAttribute("height", 48);
        r.setAttribute("rx", 9);
        r.style.stroke = corDe(n);
        if (n.nodeType === "CATEGORIA") r.setAttribute("stroke-dasharray", "5 4");
        g.appendChild(r);
        // faixa de cor à esquerda + glifo: identidade visual do tipo
        var barra = document.createElementNS(NS, "rect");
        barra.setAttribute("x", -w / 2); barra.setAttribute("y", -24);
        barra.setAttribute("width", 24); barra.setAttribute("height", 48);
        barra.setAttribute("rx", 9); barra.setAttribute("class", "no-barra");
        barra.style.fill = corDe(n);
        g.appendChild(barra);
        var ic = document.createElementNS(NS, "text");
        ic.setAttribute("x", -w / 2 + 12); ic.setAttribute("y", 5);
        ic.setAttribute("text-anchor", "middle"); ic.setAttribute("class", "no-icone");
        ic.textContent = glifoDe(n);
        g.appendChild(ic);
        var t1 = document.createElementNS(NS, "text");
        t1.setAttribute("text-anchor", "middle"); t1.setAttribute("y", -3);
        t1.setAttribute("x", 10);
        t1.textContent = n.label.length > 22 ? n.label.slice(0, 21) + "…" : n.label;
        g.appendChild(t1);
        var t2 = document.createElementNS(NS, "text");
        t2.setAttribute("text-anchor", "middle"); t2.setAttribute("y", 14);
        t2.setAttribute("x", 10);
        t2.setAttribute("class", "tipo");
        t2.textContent = (TIPOS[n.nodeType] ? TIPOS[n.nodeType].nome : "Texto") + (n.refId ? " ●" : "");
        g.appendChild(t2);
        // indicador de ramo recolhido/expansível
        var temFilhos = arestas.some(function (a) { return a.sourceId === n.id; });
        if (temFilhos) {
          var tog = document.createElementNS(NS, "circle");
          tog.setAttribute("cx", w / 2 - 12); tog.setAttribute("cy", 0); tog.setAttribute("r", 8);
          tog.setAttribute("class", "no-toggle");
          g.appendChild(tog);
          var ts = document.createElementNS(NS, "text");
          ts.setAttribute("x", w / 2 - 12); ts.setAttribute("y", 3.5);
          ts.setAttribute("text-anchor", "middle"); ts.setAttribute("class", "no-toggle-sinal");
          ts.textContent = n.collapsed ? "+" : "−";
          g.appendChild(ts);
          tog.addEventListener("mousedown", function (e) { e.stopPropagation(); });
          ts.addEventListener("mousedown", function (e) { e.stopPropagation(); });
          var alternar = function (e) {
            e.stopPropagation();
            n.collapsed = !n.collapsed;
            desenhar(); notificar();
          };
          tog.addEventListener("click", alternar);
          ts.addEventListener("click", alternar);
        }
        g.addEventListener("mousedown", function (e) {
          e.stopPropagation();
          if (origemLigacao && origemLigacao !== n.id) {
            // evita conexão duplicada ou laço
            var existe = arestas.some(function (a) {
              return a.sourceId === origemLigacao && a.targetId === n.id;
            });
            if (!existe) arestas.push({ id: uid(), sourceId: origemLigacao, targetId: n.id, label: "" });
            origemLigacao = null;
            selecionado = n.id;
            desenhar(); notificar(); atualizarPainel();
            return;
          }
          var pt = telaParaMundo(e);
          arrasto = { id: n.id, dx: n.x - pt.x, dy: n.y - pt.y, moveu: false };
        });
        g.addEventListener("click", function (e) {
          e.stopPropagation();
          if (Date.now() - ultimoArrasto < 200) return; // foi arrasto, não clique
          selecionado = n.id; arestaSelecionada = null;
          desenhar(); atualizarPainel();
        });
        g.addEventListener("dblclick", function (e) {
          e.stopPropagation();
          if (n.refType && n.refId && URLS[n.refType]) {
            window.location.href = URLS[n.refType] + n.refId;
            return;
          }
          selecionado = n.id; arestaSelecionada = null;
          desenhar(); atualizarPainel(true);
        });
        camadaNos.appendChild(g);
      });
      aplicarTransform();
    }

    function notificar() {
      var ev = new CustomEvent("editor:change", { bubbles: true });
      document.querySelector(".mapa-wrap").dispatchEvent(ev);
    }

    // ---------- painel lateral ----------
    var painel = document.getElementById("mapaPainel");
    function opcoesRef(n) {
      var tipo = TIPOS[n.nodeType];
      if (!tipo || !tipo.refs) return "";
      var lista = REFS[tipo.refs] || [];
      var html = "<div class='campo'><label>" + esc(tipo.rotulo) + "</label>" +
        "<select id='mpRef'><option value=''>— sem vínculo —</option>";
      lista.forEach(function (o) {
        html += "<option value='" + o.id + "'" + (n.refId === o.id ? " selected" : "") + ">" + esc(o.nome) + "</option>";
      });
      html += "</select>";
      if (!lista.length) html += "<small class='dica'>Nenhum registro ainda — crie na seção correspondente.</small>";
      html += "</div>";
      return html;
    }
    function atualizarPainel(focarRotulo) {
      if (!painel) return;
      var a = arestaSelecionada ? arestaPorId(arestaSelecionada) : null;
      var n = a ? null : noPorId(selecionado);
      if (a) {
        var s = noPorId(a.sourceId), t = noPorId(a.targetId);
        painel.innerHTML =
          "<p class='muted' style='margin-top:0'>Conexão: <b>" + esc(s ? s.label : "?") + "</b> → <b>" + esc(t ? t.label : "?") + "</b></p>" +
          "<div class='campo'><label for='mpArestaLabel'>Rótulo da conexão</label>" +
          "<input id='mpArestaLabel' type='text' value='" + esc(a.label || "") + "' placeholder='ex.: odeia, protege, pertence a…' maxlength='120'></div>" +
          "<div style='display:flex;gap:8px;flex-wrap:wrap'>" +
          "<button class='btn pequeno perigo' id='mpArestaExcluir'>Remover conexão</button></div>";
        document.getElementById("mpArestaLabel").addEventListener("input", function (e) { a.label = e.target.value; desenhar(); });
        document.getElementById("mpArestaLabel").addEventListener("change", notificar);
        document.getElementById("mpArestaExcluir").addEventListener("click", function () {
          arestas = arestas.filter(function (x) { return x !== a; });
          arestaSelecionada = null;
          desenhar(); atualizarPainel(); notificar();
        });
        return;
      }
      if (!n) {
        painel.innerHTML =
          "<p class='muted' style='margin-top:0'>Nada selecionado.</p>" +
          "<ul class='mapa-dicas'>" +
          "<li>Duplo-clique no fundo: cria um nó</li>" +
          "<li>Arraste um nó para mover · arraste o fundo para navegar</li>" +
          "<li><b>Ligar</b> conecta ao próximo nó clicado</li>" +
          "<li>Duplo-clique num nó vinculado abre o registro</li>" +
          "<li><kbd>Delete</kbd> remove o selecionado · <kbd>Esc</kbd> cancela</li>" +
          "</ul>";
        return;
      }
      var html =
        "<div class='campo'><label for='mpLabel'>Rótulo</label><input id='mpLabel' type='text' value='" + esc(n.label) + "' maxlength='200'></div>" +
        "<div class='form-linha'><div class='campo'><label for='mpTipo'>Tipo</label><select id='mpTipo'>";
      Object.keys(TIPOS).forEach(function (k) {
        html += "<option value='" + k + "'" + (n.nodeType === k ? " selected" : "") + ">" + TIPOS[k].glifo + " " + TIPOS[k].nome + "</option>";
      });
      html += "</select></div>" +
        "<div class='campo'><label>Cor</label><div class='amostras' id='mpCores'>" +
        CORES.map(function (c) {
          return "<button type='button' class='amostra" + ((n.color || "") === c ? " ativo" : "") + "' data-cor='" + c + "' " +
            "style='background:" + (c || "transparent") + (c ? "" : ";border-style:dashed") + "' title='" + (c || "Cor do tipo") + "' aria-label='Cor " + (c || "padrão") + "'></button>";
        }).join("") + "</div></div></div>";
      html += opcoesRef(n);
      html += "<div class='campo'><label for='mpConteudo'>Notas do nó</label><textarea id='mpConteudo' rows='3'>" + esc(n.content || "") + "</textarea></div>";
      html += "<div style='display:flex;gap:8px;flex-wrap:wrap'>";
      if (n.refType && n.refId && URLS[n.refType]) {
        html += "<a class='btn pequeno primario' href='" + URLS[n.refType] + n.refId + "'>Abrir registro ↗</a>";
      }
      html += "<button class='btn pequeno' id='mpLigar'>⇢ Ligar a outro nó</button>";
      var temFilhos = arestas.some(function (x) { return x.sourceId === n.id; });
      if (temFilhos) html += "<button class='btn pequeno fantasma' id='mpRecolher'>" + (n.collapsed ? "Expandir ramo" : "Recolher ramo") + "</button>";
      html += "<button class='btn pequeno perigo' id='mpExcluir'>Excluir nó</button></div>";
      painel.innerHTML = html;

      var inpLabel = document.getElementById("mpLabel");
      inpLabel.addEventListener("input", function (e) { n.label = e.target.value; desenhar(); });
      inpLabel.addEventListener("change", notificar);
      if (focarRotulo) { inpLabel.focus(); inpLabel.select(); }
      document.getElementById("mpTipo").addEventListener("change", function (e) {
        n.nodeType = e.target.value;
        // trocou de tipo: a referência antiga deixa de fazer sentido
        n.refType = null; n.refId = null;
        desenhar(); atualizarPainel(); notificar();
      });
      document.getElementById("mpConteudo").addEventListener("change", function (e) { n.content = e.target.value; notificar(); });
      document.getElementById("mpCores").addEventListener("click", function (e) {
        var b = e.target.closest(".amostra");
        if (!b) return;
        n.color = b.getAttribute("data-cor");
        desenhar(); atualizarPainel(); notificar();
      });
      var selRef = document.getElementById("mpRef");
      if (selRef) selRef.addEventListener("change", function () {
        if (selRef.value) {
          n.refType = REF_POR_TIPO[n.nodeType];
          n.refId = selRef.value;
          var escolha = (REFS[TIPOS[n.nodeType].refs] || []).find(function (o) { return o.id === selRef.value; });
          if (escolha && (!n.label || n.label === "Nova ideia" || n.label === "Nó")) n.label = escolha.nome;
        } else {
          n.refType = null; n.refId = null;
        }
        desenhar(); atualizarPainel(); notificar();
      });
      document.getElementById("mpLigar").addEventListener("click", function () {
        origemLigacao = n.id; arestaSelecionada = null;
        desenhar(); atualizarPainel();
        painel.insertAdjacentHTML("beforeend",
          "<p class='muted' id='mpLigando'>Modo ligação: clique no nó de destino. <kbd>Esc</kbd> cancela.</p>");
      });
      var btnRecolher = document.getElementById("mpRecolher");
      if (btnRecolher) btnRecolher.addEventListener("click", function () {
        n.collapsed = !n.collapsed;
        desenhar(); atualizarPainel(); notificar();
      });
      document.getElementById("mpExcluir").addEventListener("click", function () {
        if (!window.confirm("Excluir este nó e suas conexões?")) return;
        nos = nos.filter(function (x) { return x.id !== n.id; });
        arestas = arestas.filter(function (x) { return x.sourceId !== n.id && x.targetId !== n.id; });
        selecionado = null;
        desenhar(); atualizarPainel(); notificar();
      });
    }

    // ---------- interações de canvas ----------
    svg.addEventListener("mousedown", function (e) {
      arrasto = arrasto || { pan: true, sx: e.clientX, sy: e.clientY, px: pan.x, py: pan.y };
    });
    window.addEventListener("mousemove", function (e) {
      if (!arrasto) return;
      if (arrasto.pan) {
        var rect = svg.getBoundingClientRect();
        var escala = rect.width / svg.viewBox.baseVal.width || 1;
        pan.x = arrasto.px + (e.clientX - arrasto.sx) / escala;
        pan.y = arrasto.py + (e.clientY - arrasto.sy) / escala;
        aplicarTransform();
      } else if (arrasto.id) {
        var n = noPorId(arrasto.id);
        if (n) {
          var p = telaParaMundo(e);
          var nx = Math.round(p.x + arrasto.dx), ny = Math.round(p.y + arrasto.dy);
          if (nx !== n.x || ny !== n.y) { n.x = nx; n.y = ny; arrasto.moveu = true; desenhar(); }
        }
      }
    });
    window.addEventListener("mouseup", function () {
      if (arrasto && arrasto.id && arrasto.moveu) { notificar(); ultimoArrasto = Date.now(); }
      else if (arrasto && arrasto.pan) ultimoArrasto = Date.now();
      arrasto = null;
    });
    svg.addEventListener("click", function () {
      if (Date.now() - ultimoArrasto < 200) return; // soltou após arrastar: não desseleciona
      if (origemLigacao) { origemLigacao = null; desenhar(); atualizarPainel(); return; }
      if (selecionado || arestaSelecionada) {
        selecionado = null; arestaSelecionada = null;
        desenhar(); atualizarPainel();
      }
    });
    svg.addEventListener("wheel", function (e) {
      e.preventDefault();
      // zoom ancorado no cursor: o ponto sob o mouse permanece no lugar
      var pt = telaParaMundo(e);
      var nz = Math.min(2.5, Math.max(0.25, pan.z * (e.deltaY < 0 ? 1.1 : 0.9)));
      pan.x = pan.x + pt.x * (pan.z - nz);
      pan.y = pan.y + pt.y * (pan.z - nz);
      pan.z = nz;
      aplicarTransform();
    }, { passive: false });
    svg.addEventListener("dblclick", function (e) {
      var p = telaParaMundo(e);
      var novo = { id: uid(), nodeType: "TEXTO", label: "Nova ideia", content: "", refType: null, refId: null,
        x: Math.round(p.x), y: Math.round(p.y), color: "", collapsed: false };
      nos.push(novo);
      selecionado = novo.id;
      desenhar(); atualizarPainel(true); notificar();
    });
    // teclado: Delete remove seleção; Esc cancela ligação/seleção
    document.addEventListener("keydown", function (e) {
      var digitando = e.target && (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA" || e.target.isContentEditable);
      if (digitando) return;
      if ((e.key === "Delete" || e.key === "Backspace") && selecionado) {
        var n = noPorId(selecionado);
        if (n) {
          nos = nos.filter(function (x) { return x.id !== n.id; });
          arestas = arestas.filter(function (x) { return x.sourceId !== n.id && x.targetId !== n.id; });
          selecionado = null;
          desenhar(); atualizarPainel(); notificar();
        }
      } else if (e.key === "Escape") {
        origemLigacao = null; selecionado = null; arestaSelecionada = null;
        desenhar(); atualizarPainel();
      }
    });

    // ---------- barra de ferramentas ----------
    function ajustarVisao() {
      var vis = nos.filter(function (n) { return !ocultos()[n.id]; });
      if (!vis.length) { pan = { x: 40, y: 40, z: 1 }; aplicarTransform(); return; }
      var minX = Math.min.apply(null, vis.map(function (n) { return n.x - larguraDe(n) / 2; }));
      var maxX = Math.max.apply(null, vis.map(function (n) { return n.x + larguraDe(n) / 2; }));
      var minY = Math.min.apply(null, vis.map(function (n) { return n.y - 40; }));
      var maxY = Math.max.apply(null, vis.map(function (n) { return n.y + 40; }));
      var bw = Math.max(1, maxX - minX), bh = Math.max(1, maxY - minY);
      pan.z = Math.min(2.5, Math.max(0.25, Math.min(940 / bw, 500 / bh)));
      pan.x = (1000 - bw * pan.z) / 2 - minX * pan.z;
      pan.y = (560 - bh * pan.z) / 2 - minY * pan.z;
      aplicarTransform();
    }
    function organizar() {
      // layout em camadas (BFS a partir das raízes); ilhas empilhadas à direita
      var filhos = {}, entradas = {};
      nos.forEach(function (n) { filhos[n.id] = []; entradas[n.id] = 0; });
      arestas.forEach(function (a) {
        if (filhos[a.sourceId] && filhos[a.targetId] !== undefined) {
          filhos[a.sourceId].push(a.targetId);
          entradas[a.targetId]++;
        }
      });
      var raizes = nos.filter(function (n) { return entradas[n.id] === 0; }).map(function (n) { return n.id; });
      if (!raizes.length && nos.length) raizes = [nos[0].id];
      var camada = {};
      var fila = raizes.slice();
      raizes.forEach(function (r) { camada[r] = 0; });
      while (fila.length) {
        var cur = fila.shift();
        filhos[cur].forEach(function (f) {
          var nv = camada[cur] + 1;
          if (camada[f] === undefined || nv > camada[f]) { camada[f] = nv; fila.push(f); }
        });
      }
      var porCamada = {};
      nos.forEach(function (n) {
        var c = camada[n.id] === undefined ? 0 : camada[n.id];
        (porCamada[c] = porCamada[c] || []).push(n.id);
      });
      Object.keys(porCamada).forEach(function (c) {
        var ids = porCamada[c];
        ids.forEach(function (id, i) {
          var n = noPorId(id);
          if (!n) return;
          n.x = 160 + Number(c) * 260;
          n.y = Math.round(120 + i * 110 + (Number(c) % 2) * 55);
        });
      });
      desenhar(); ajustarVisao(); notificar();
    }
    var btnCentralizar = document.getElementById("mapaCentralizar");
    if (btnCentralizar) btnCentralizar.addEventListener("click", ajustarVisao);
    var btnOrganizar = document.getElementById("mapaOrganizar");
    if (btnOrganizar) btnOrganizar.addEventListener("click", organizar);
    var btnZoomMais = document.getElementById("mapaZoomMais");
    if (btnZoomMais) btnZoomMais.addEventListener("click", function () { pan.z = Math.min(2.5, pan.z * 1.15); aplicarTransform(); });
    var btnZoomMenos = document.getElementById("mapaZoomMenos");
    if (btnZoomMenos) btnZoomMenos.addEventListener("click", function () { pan.z = Math.max(0.25, pan.z / 1.15); aplicarTransform(); });
    var btnNovoNo = document.getElementById("mapaNovoNo");
    if (btnNovoNo) btnNovoNo.addEventListener("click", function () {
      var p = telaParaMundoCentro();
      var novo = { id: uid(), nodeType: "TEXTO", label: "Nova ideia", content: "", refType: null, refId: null,
        x: p.x + Math.round(Math.random() * 80 - 40), y: p.y + Math.round(Math.random() * 80 - 40), color: "", collapsed: false };
      nos.push(novo);
      selecionado = novo.id;
      desenhar(); atualizarPainel(true); notificar();
    });
    function telaParaMundoCentro() {
      var rect = svg.getBoundingClientRect();
      return telaParaMundo({ clientX: rect.left + rect.width / 2, clientY: rect.top + rect.height / 2 });
    }

    // coleta para o autosave (contrato com autosave.js)
    window.salomaoMapaColetar = function () {
      return {
        nos: nos.map(function (n) {
          return { id: n.id, nodeType: n.nodeType, label: (n.label || ""), content: n.content || "",
            refType: n.refType, refId: n.refId, x: n.x, y: n.y, color: n.color || "", collapsed: !!n.collapsed };
        }),
        arestas: arestas.map(function (a) { return { id: a.id, sourceId: a.sourceId, targetId: a.targetId, label: a.label || "" }; })
      };
    };

    // carrega grafo persistido
    window.salomaoFetch(grafoUrl).then(function (g) {
      nos = (g.nos || []).map(function (n) {
        return { id: n.id, nodeType: TIPOS[n.nodeType] ? n.nodeType : "TEXTO", label: n.label, content: n.content || "",
          refType: n.refType, refId: n.refId, x: n.x, y: n.y, color: n.color || "", collapsed: !!n.collapsed };
      });
      arestas = (g.arestas || []).map(function (a) {
        return { id: a.id || uid(), sourceId: a.sourceId, targetId: a.targetId, label: a.label || "" };
      });
      desenhar(); atualizarPainel(); ajustarVisao();
    }).catch(function () {
      if (painel) painel.innerHTML = "<p class='muted'>Não foi possível carregar o mapa.</p>";
    });
    atualizarPainel();
  });
})();

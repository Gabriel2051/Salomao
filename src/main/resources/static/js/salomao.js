/* SALOMÃO — interações globais: menu mobile, paleta Ctrl+K, modal de poder, confirmações. */
(function () {
  "use strict";

  function csrf() {
    var t = document.querySelector('meta[name="_csrf"]');
    var h = document.querySelector('meta[name="_csrf_header"]');
    return { token: t ? t.content : "", header: h ? h.content : "X-CSRF-TOKEN" };
  }
  window.salomaoCsrf = csrf;

  async function fetchJson(url, options) {
    options = options || {};
    options.headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
    var c = csrf();
    if (c.token) options.headers[c.header] = c.token;
    var r = await fetch(url, options);
    if (!r.ok) throw new Error("HTTP " + r.status);
    return r.json();
  }
  window.salomaoFetch = fetchJson;

  // ---- nav ativa por URL (funciona em subpaginas: form, detalhe, edicao) ----
  document.addEventListener("DOMContentLoaded", function () {
    var path = window.location.pathname;
    document.querySelectorAll(".nav a").forEach(function (a) {
      var href = a.getAttribute("href");
      if (href && (path === href || path.indexOf(href + "/") === 0)) {
        a.classList.add("ativo");
        a.setAttribute("aria-current", "page");
      }
    });
  });

  // ---- menu mobile ----
  function definirMenu(aberto) {
    document.body.classList.toggle("menu-aberto", aberto);
    ["menuBtn", "menuBtn2"].forEach(function (mid) {
      var bt = document.getElementById(mid);
      if (bt) {
        bt.setAttribute("aria-expanded", aberto ? "true" : "false");
        bt.setAttribute("aria-label", aberto ? "Fechar navegação" : "Abrir navegação");
      }
    });
  }
  ["menuBtn", "menuBtn2"].forEach(function (id) {
    var b = document.getElementById(id);
    if (b) b.addEventListener("click", function () {
      definirMenu(!document.body.classList.contains("menu-aberto"));
    });
  });
  // clique no scrim (pseudo-elemento ::after): evento chega com target = body
  document.addEventListener("click", function (e) {
    if (document.body.classList.contains("menu-aberto") && e.target === document.body) definirMenu(false);
  });
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && document.body.classList.contains("menu-aberto")) definirMenu(false);
  });
  var sb = document.getElementById("sidebar");
  if (sb) sb.addEventListener("click", function (e) {
    if (e.target.closest("a")) definirMenu(false);
  });

  // ---- confirmação de exclusão ----
  document.addEventListener("submit", function (e) {
    var f = e.target;
    if (f.matches("[data-confirmar]")) {
      var msg = f.getAttribute("data-confirmar") || "Excluir? Esta ação não pode ser desfeita.";
      if (!window.confirm(msg)) e.preventDefault();
    }
  });

  // ---- mostrar/ocultar senha ----
  document.addEventListener("click", function (e) {
    var b = e.target.closest("[data-mostra-senha]");
    if (!b) return;
    var input = document.getElementById(b.getAttribute("data-mostra-senha"));
    if (!input) return;
    input.type = input.type === "password" ? "text" : "password";
    b.textContent = input.type === "password" ? "mostrar" : "ocultar";
  });

  // ---- estado de carregamento nos formulários de auth ----
  document.addEventListener("submit", function (e) {
    var f = e.target;
    if (f.matches && f.matches(".auth-caixa form")) {
      var b = f.querySelector("[type=submit]");
      if (b && !b.disabled) { b.disabled = true; b.textContent = "Aguarde…"; }
    }
  });

  // ---- paleta global (Ctrl+K) ----
  var paleta = document.getElementById("paleta");
  var paletaInput = document.getElementById("paletaInput");
  var paletaLista = document.getElementById("paletaLista");
  var buscaBtn = document.getElementById("buscaGlobal");
  var itens = [];
  var sel = 0;

  function abrirPaleta() {
    if (!paleta) return;
    paleta.hidden = false;
    paletaInput.value = "";
    paletaLista.innerHTML = "";
    itens = []; sel = 0;
    setTimeout(function () { paletaInput.focus(); }, 30);
  }
  function fecharPaleta() { if (paleta) paleta.hidden = true; }

  if (buscaBtn) buscaBtn.addEventListener("click", abrirPaleta);
  document.addEventListener("keydown", function (e) {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k") { e.preventDefault(); abrirPaleta(); }
    if (e.key === "Escape") { fecharPaleta(); fecharModal(); }
    if (paleta && !paleta.hidden && (e.key === "ArrowDown" || e.key === "ArrowUp")) {
      e.preventDefault();
      sel = e.key === "ArrowDown" ? Math.min(sel + 1, itens.length - 1) : Math.max(sel - 1, 0);
      destacar();
    }
    if (paleta && !paleta.hidden && e.key === "Enter" && itens[sel]) {
      window.location.href = itens[sel].url;
    }
  });
  if (paleta) paleta.addEventListener("click", function (e) { if (e.target === paleta) fecharPaleta(); });

  var debounce;
  if (paletaInput) paletaInput.addEventListener("input", function () {
    clearTimeout(debounce);
    var q = paletaInput.value.trim();
    if (q.length < 2) { paletaLista.innerHTML = ""; itens = []; return; }
    debounce = setTimeout(function () {
      fetchJson("/api/pesquisa?q=" + encodeURIComponent(q)).then(function (d) {
        itens = d.itens || []; sel = 0; renderPaleta();
      }).catch(function () { paletaLista.innerHTML = "<li class='muted'>Não foi possível pesquisar.</li>"; });
    }, 220);
  });

  function renderPaleta() {
    paletaLista.innerHTML = "";
    if (!itens.length) { paletaLista.innerHTML = "<li class='muted' style='padding:8px'>Nenhum resultado.</li>"; return; }
    itens.forEach(function (it, i) {
      var li = document.createElement("li");
      var a = document.createElement("a");
      a.href = it.url;
      if (i === sel) a.classList.add("ativo");
      var tag = document.createElement("span");
      tag.className = "etiqueta"; tag.textContent = it.tipo;
      var nome = document.createElement("span");
      nome.textContent = it.nome;
      a.appendChild(tag); a.appendChild(nome); li.appendChild(a);
      paletaLista.appendChild(li);
    });
  }
  function destacar() {
    Array.prototype.forEach.call(paletaLista.querySelectorAll("a"), function (a, i) {
      a.classList.toggle("ativo", i === sel);
      if (i === sel) a.scrollIntoView({ block: "nearest" });
    });
  }

  // ---- modal de poder (tokens clicáveis) ----
  // Teclado: spans com tabindex abrem o modal com Enter/Espaço (botões já são nativos).
  document.addEventListener("keydown", function (e) {
    if ((e.key === "Enter" || e.key === " ") && e.target.matches &&
        e.target.matches("span[data-poder-id]")) {
      e.preventDefault();
      e.target.click();
    }
  });
  var modal = document.getElementById("poderModal");
  var corpo = document.getElementById("poderModalCorpo");
  function fecharModal() { if (modal) modal.hidden = true; }
  document.addEventListener("click", function (e) {
    var f = e.target.closest("[data-fechar]");
    if (f) fecharModal();
    if (e.target === modal) fecharModal();
    var tok = e.target.closest("[data-poder-id]");
    if (!tok || e.target.closest("button")) return;
    var id = tok.getAttribute("data-poder-id");
    if (modal && corpo && id) {
      modal.hidden = false;
      corpo.innerHTML = "<p class='muted'>Carregando…</p>";
      fetchJson("/poderes/api/" + id).then(function (p) {
        var html = "<h2 style='margin-top:0'>✦ " + esc(p.nome) + "</h2>";
        if (p.categoria || p.nivel) html += "<p class='muted'>" + esc(p.categoria || "") + (p.categoria && p.nivel ? " · " : "") + esc(p.nivel || "") + "</p>";
        if (p.descricao) html += "<div>" + p.descricao + "</div>";
        if (p.personagens && p.personagens.length) {
          html += "<h3>Personagens que possuem este poder</h3><div class='tokens'>";
          p.personagens.forEach(function (c) {
            html += "<a class='token' href='/personagens/" + esc(c.id) + "'>" + esc(c.nome) + "</a>";
          });
          html += "</div>";
        }
        html += "<p style='margin-top:14px'><a class='btn pequeno' href='" + esc(p.url) + "/editar'>Editar poder</a> <a class='btn pequeno fantasma' href='" + esc(p.url) + "'>Abrir página</a></p>";
        corpo.innerHTML = html;
      }).catch(function () { corpo.innerHTML = "<p class='muted'>Não foi possível carregar o poder.</p>"; });
    }
  });

  function esc(s) {
    return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }
  window.salomaoEsc = esc;
})();

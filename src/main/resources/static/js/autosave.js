/* SALOMÃO — autosave com debounce + indicador visual + proteção contra sobrescrita. */
(function () {
  "use strict";

  function estado(msg, classe, podeRepetir) {
    var el = document.getElementById("saveState");
    if (el) {
      el.textContent = msg;
      el.className = "save-state " + (classe || "");
      el.style.cursor = podeRepetir ? "pointer" : "";
      el.title = podeRepetir ? "Clique para tentar novamente" : "";
      el.onclick = podeRepetir ? function () { salvar(); } : null;
    }
    var local = document.querySelector("[data-autosave-estado]");
    if (local) {
      local.textContent = msg;
      local.className = classe || "";
      local.style.cursor = podeRepetir ? "pointer" : "";
      local.title = podeRepetir ? "Clique para tentar novamente" : "";
      local.onclick = podeRepetir ? function () { salvar(); } : null;
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    var root = document.querySelector("[data-autosave]");
    if (!root) return;
    var url = root.getAttribute("data-autosave");
    var intervalo = parseInt(root.getAttribute("data-autosave-intervalo") || "1500", 10);
    var coletar = root.getAttribute("data-autosave-coletar") || "form";
    var timer = null;
    var salvando = false;
    var pendente = false;
    var ultimoOk = "";

    function coletarDados() {
      var dados = {};
      if (coletar === "grafo" && window.salomaoMapaColetar) {
        return window.salomaoMapaColetar();
      }
      root.querySelectorAll("[data-autosave-campo]").forEach(function (el) {
        dados[el.getAttribute("data-autosave-campo")] = el.value;
      });
      // menções do editor de histórias
      var m = root.querySelector("[data-mencoes-ids]");
      if (m) {
        try { dados.mencoes = JSON.parse(m.value || "[]"); }
        catch (e) { dados.mencoes = []; }
      }
      return dados;
    }

    function assinatura(dados) { return JSON.stringify(dados); }

    function salvar() {
      var dados = coletarDados();
      var atual = assinatura(dados);
      if (atual === ultimoOk) return; // nada mudou
      if (salvando) { pendente = true; return; }
      salvando = true;
      estado("Salvando…", "salvando");
      window.salomaoFetch(url, { method: "PUT", body: JSON.stringify(dados) }).then(function (r) {
        salvando = false;
        if (r && r.ok) {
          ultimoOk = assinatura(coletarDados());
          var q = r.quando ? new Date(r.quando) : new Date();
          estado("✓ Salvo " + q.toLocaleTimeString("pt-BR"), "salvo");
        } else {
          estado("Não foi possível salvar. Clique para tentar novamente.", "erro", true);
        }
        if (pendente) { pendente = false; salvar(); }
      }).catch(function () {
        salvando = false;
        estado("Não foi possível salvar. Clique para tentar novamente.", "erro", true);
      });
    }

    function agendar() {
      estado("Alterações não salvas…", "");
      clearTimeout(timer);
      timer = setTimeout(salvar, intervalo);
    }

    root.addEventListener("editor:change", agendar);
    root.addEventListener("input", function (e) {
      if (e.target.matches("[data-autosave-campo]")) agendar();
    });
    root.addEventListener("change", function (e) {
      if (e.target.matches("[data-autosave-campo]")) salvar();
    });
    var tentar = document.querySelector("[data-autosave-tentar]");
    if (tentar) tentar.addEventListener("click", salvar);

    ultimoOk = assinatura(coletarDados());
    estado("✓ Salvo", "salvo");
  });
})();

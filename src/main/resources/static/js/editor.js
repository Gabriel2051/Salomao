/* SALOMÃO — editor rico: toolbar (negrito/itálico/sublinhado/títulos/listas/citação/link)
   + menções @Personagem com autocomplete + sincronização para input hidden. */
(function () {
  "use strict";

  function initEditor(root) {
    var area = root.querySelector("[data-editor-area]");
    var hidden = root.querySelector("[data-editor-hidden]");
    var mencionaUrl = root.getAttribute("data-menciona-url");
    if (!area || !hidden) return;
    area.innerHTML = hidden.value || "";

    // aplica estado inicial dos botões
    root.querySelectorAll("[data-cmd]").forEach(function (btn) {
      btn.addEventListener("click", function (e) {
        e.preventDefault();
        area.focus();
        var cmd = btn.getAttribute("data-cmd");
        var val = btn.getAttribute("data-valor") || null;
        if (cmd === "createLink") {
          var url = window.prompt("URL do link (https://…):", "https://");
          if (!url) return;
          document.execCommand("createLink", false, url);
        } else if (cmd === "insertUnorderedList" || cmd === "insertOrderedList") {
          document.execCommand(cmd, false, null);
        } else {
          document.execCommand(cmd, false, val);
        }
        sync();
        atualizarBotoes();
      });
    });
    document.addEventListener("selectionchange", function () {
      if (document.activeElement === area) atualizarBotoes();
    });
    function atualizarBotoes() {
      root.querySelectorAll("[data-cmd]").forEach(function (btn) {
        try {
          var cmd = btn.getAttribute("data-cmd");
          if (["bold", "italic", "underline", "insertUnorderedList", "insertOrderedList"].indexOf(cmd) >= 0) {
            btn.classList.toggle("ativo", document.queryCommandState(cmd));
          }
        } catch (err) { /* noop */ }
      });
    }

    function sync() { hidden.value = area.innerHTML; root.dispatchEvent(new CustomEvent("editor:change")); }

    ["input", "paste", "keyup", "mouseup"].forEach(function (ev) {
      area.addEventListener(ev, function () { sync(); });
    });
    // cola como texto quando possível, preservando quebras básicas
    area.addEventListener("paste", function (e) {
      if (!e.clipboardData) return;
      var html = e.clipboardData.getData("text/html");
      if (!html) return; // deixa o navegador colar texto puro
      e.preventDefault();
      document.execCommand("insertHTML", false, html);
      sync();
    });

    // garante submit do form com conteúdo atual
    var form = root.closest("form");
    if (form) form.addEventListener("submit", sync);

    // Menções vindas do banco perdem os listeners ao recarregar o HTML:
    // delegação garante que TODA menção (nova ou recarregada) seja clicável.
    area.addEventListener("click", function (e) {
      var m = e.target.closest ? e.target.closest(".mencao[data-mention-id]") : null;
      if (m) window.location.href = "/personagens/" + m.getAttribute("data-mention-id");
    });

    // ---- menções @ ----
    if (mencionaUrl) initMencoes(root, area, mencionaUrl, sync);
  }

  function initMencoes(root, area, url, sync) {
    var box = document.createElement("ul");
    box.className = "autocomplete";
    box.hidden = true;
    box.setAttribute("role", "listbox");
    document.body.appendChild(box);
    var resultados = [];
    var indice = 0;
    var termo = null;

    area.addEventListener("keyup", function () {
      var sel = window.getSelection();
      if (!sel.rangeCount) { box.hidden = true; return; }
      var range = sel.getRangeAt(0);
      var antes = range.startContainer.textContent
        ? range.startContainer.textContent.slice(0, range.startOffset) : "";
      var m = antes.match(/@([\p{L}\p{N} ]{1,30})$/u);
      if (!m || !area.contains(range.startContainer)) { box.hidden = true; return; }
      termo = { texto: m[1], range: range.cloneRange() };
      buscar(m[1]);
    });

    var deb;
    function buscar(q) {
      clearTimeout(deb);
      deb = setTimeout(function () {
        window.salomaoFetch(url + "?q=" + encodeURIComponent(q)).then(function (lista) {
          resultados = lista || []; indice = 0;
          if (!resultados.length) { box.hidden = true; return; }
          render();
        }).catch(function () { box.hidden = true; });
      }, 180);
    }

    function render() {
      var r = termo.range.getBoundingClientRect();
      box.style.left = Math.min(window.innerWidth - 260, r.left + window.scrollX) + "px";
      box.style.top = (r.bottom + window.scrollY + 6) + "px";
      box.innerHTML = "";
      resultados.forEach(function (c, i) {
        var li = document.createElement("li");
        li.textContent = c.nome;
        li.setAttribute("role", "option");
        if (i === indice) li.classList.add("ativo");
        li.addEventListener("mousedown", function (e) { e.preventDefault(); aplicar(c); });
        box.appendChild(li);
      });
      box.hidden = false;
    }

    area.addEventListener("keydown", function (e) {
      if (box.hidden) return;
      if (e.key === "ArrowDown") { e.preventDefault(); indice = Math.min(indice + 1, resultados.length - 1); render(); }
      else if (e.key === "ArrowUp") { e.preventDefault(); indice = Math.max(indice - 1, 0); render(); }
      else if (e.key === "Enter" && resultados[indice]) { e.preventDefault(); aplicar(resultados[indice]); }
      else if (e.key === "Escape") { box.hidden = true; }
    });

    function aplicar(c) {
      box.hidden = true;
      var sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(termo.range);
      // apaga o "@termo" digitado
      for (var i = 0; i < termo.texto.length + 1; i++) {
        document.execCommand("delete", false, null);
      }
      var span = document.createElement("span");
      span.className = "mencao";
      span.setAttribute("data-mention-id", c.id);
      span.setAttribute("data-mention-type", "CHARACTER");
      span.textContent = "@" + c.nome;
      span.title = "Abrir personagem (clique)";
      var range = sel.getRangeAt(0);
      range.insertNode(span);
      range.setStartAfter(span);
      range.insertNode(document.createTextNode(" "));
      range.collapse(false);
      sel.removeAllRanges();
      sel.addRange(range);
      // registra id para o backend persistir story_character_mentions
      var ids = root.querySelector("[data-mencoes-ids]");
      var atuais = [];
      try { atuais = JSON.parse(ids.value || "[]"); } catch (err) { atuais = []; }
      if (ids && atuais.indexOf(c.id) < 0) { atuais.push(c.id); ids.value = JSON.stringify(atuais); }
      sync();
      area.focus();
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-editor]").forEach(initEditor);
  });
})();

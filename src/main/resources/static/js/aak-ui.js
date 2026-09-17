(function () {
  function ready(callback) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", callback);
      return;
    }

    callback();
  }

  ready(function () {
    if (!document.querySelector(".aak-skip-link")) {
      var skipLink = document.createElement("a");
      skipLink.className = "aak-skip-link";
      skipLink.href = "#mainContent";
      skipLink.textContent = "Skip to content";
      document.body.prepend(skipLink);
    }

    var main =
      document.querySelector("main") ||
      document.querySelector(".content") ||
      document.querySelector(".container");

    if (main && !main.id) {
      main.id = "mainContent";
    }

    var path = window.location.pathname.replace(/\/$/, "") || "/";
    document.querySelectorAll(".navigation a[href]").forEach(function (link) {
      var linkPath = new URL(link.getAttribute("href"), window.location.origin)
        .pathname.replace(/\/$/, "") || "/";

      if (linkPath === path || (linkPath !== "/" && path.indexOf(linkPath) === 0)) {
        link.classList.add("active");
      }
    });

    document.querySelectorAll(".table-container table").forEach(function (table) {
      var bodyRows = table.querySelectorAll("tbody tr");
      var host = table.closest(".table-container");

      if (!host || host.querySelector(".aak-table-count") || !bodyRows.length) {
        return;
      }

      var count = document.createElement("p");
      count.className = "aak-table-count";
      count.textContent = bodyRows.length + (bodyRows.length === 1 ? " record" : " records");
      host.prepend(count);
    });

    document.querySelectorAll("form.delete-form, form[action*='/delete/']").forEach(function (form) {
      if (form.dataset.aakConfirmBound === "true" || form.hasAttribute("onsubmit")) {
        return;
      }

      form.dataset.aakConfirmBound = "true";
      form.addEventListener("submit", function (event) {
        if (!window.confirm("Delete this record? This action cannot be undone.")) {
          event.preventDefault();
        }
      });
    });

    document.addEventListener("keydown", function (event) {
      var isSearchShortcut =
        (event.ctrlKey || event.metaKey) &&
        event.key.toLowerCase() === "k";

      if (!isSearchShortcut) {
        return;
      }

      var search =
        document.querySelector("input[type='search']") ||
        document.querySelector("input[id*='Search']") ||
        document.querySelector(".search-wrapper input");

      if (search) {
        event.preventDefault();
        search.focus();
        search.select();
      }
    });
  });
})();

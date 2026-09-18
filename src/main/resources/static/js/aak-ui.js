(function () {
  function ready(callback) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", callback);
      return;
    }

    callback();
  }

  // Applied as early as this script executes (before DOMContentLoaded) to limit theme flash.
  var THEME_KEY = "aak-theme";
  var storedTheme = null;

  try {
    storedTheme = window.localStorage.getItem(THEME_KEY);
  } catch (error) {
    storedTheme = null;
  }

  if (storedTheme === "dark" || storedTheme === "light") {
    document.documentElement.setAttribute("data-theme", storedTheme);
  }

  function currentTheme() {
    return document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
  }

  function setTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);

    try {
      window.localStorage.setItem(THEME_KEY, theme);
    } catch (error) {
      /* localStorage unavailable (private mode etc.) - theme just won't persist */
    }

    var toggle = document.querySelector(".aak-theme-toggle");

    if (toggle) {
      toggle.textContent = theme === "dark" ? "☀️" : "🌙";
      toggle.setAttribute(
        "aria-label",
        theme === "dark" ? "Switch to light mode" : "Switch to dark mode"
      );
    }
  }

  var BOTTOM_NAV_LINKS = [
    { href: "/", label: "Dashboard", icon: "🏠" },
    { href: "/customers", label: "Customers", icon: "👥" },
    { href: "/sales-invoices", label: "Sales", icon: "🧾" },
    { href: "/payments", label: "Payments", icon: "💳" }
  ];

  var MORE_LINKS = [
    { href: "/products", label: "Products", icon: "📦" },
    { href: "/purchase-invoices", label: "Purchase Invoices", icon: "📥" },
    { href: "/inventory", label: "Inventory", icon: "📊" },
    { href: "/inventory/adjustments", label: "Stock Adjustments", icon: "🛠️" },
    { href: "/cheques", label: "Cheques", icon: "🏦" },
    { href: "/collections", label: "Collections", icon: "📬" },
    { href: "/reports", label: "Reports", icon: "📈" },
    { href: "/employees", label: "Employees", icon: "🧑‍💼" },
    { href: "/vehicles", label: "Vehicles", icon: "🚚" },
    { href: "/routes", label: "Routes", icon: "🗺️" },
    { href: "/delivery-trips", label: "Delivery Trips", icon: "🚛" },
    { href: "/delivery-trips/vehicle-stock", label: "Vehicle Stock", icon: "📦" },
    { href: "/users", label: "Manage Users", icon: "🔑" },
    { href: "/account/change-password", label: "Change Password", icon: "🔒" },
    { href: "/logout", label: "Logout", icon: "🚪" }
  ];

  function normalizedPath(href) {
    return (
      new URL(href, window.location.origin).pathname.replace(/\/$/, "") || "/"
    );
  }

  function isActivePath(href) {
    var linkPath = normalizedPath(href);
    var path = normalizedPath(window.location.pathname);
    return linkPath === path || (linkPath !== "/" && path.indexOf(linkPath) === 0);
  }

  function buildThemeToggle() {
    if (document.querySelector(".aak-theme-toggle")) {
      return;
    }

    var button = document.createElement("button");
    button.type = "button";
    button.className = "aak-theme-toggle";

    button.addEventListener("click", function () {
      setTheme(currentTheme() === "dark" ? "light" : "dark");
    });

    document.body.appendChild(button);
    setTheme(currentTheme());
  }

  function buildBottomNav() {
    if (document.querySelector(".aak-bottom-nav")) {
      return;
    }

    document.body.classList.add("aak-has-bottom-nav");

    var nav = document.createElement("nav");
    nav.className = "aak-bottom-nav";
    nav.setAttribute("aria-label", "Primary");

    BOTTOM_NAV_LINKS.forEach(function (item) {
      var link = document.createElement("a");
      link.href = item.href;

      if (isActivePath(item.href)) {
        link.classList.add("active");
      }

      link.innerHTML =
        '<span class="aak-bottom-nav-icon" aria-hidden="true">' + item.icon + "</span>" +
        "<span>" + item.label + "</span>";

      nav.appendChild(link);
    });

    var sheet = document.createElement("div");
    sheet.className = "aak-bottom-nav-more-sheet";

    var panel = document.createElement("div");
    panel.className = "aak-bottom-nav-more-sheet-panel";
    panel.innerHTML = '<div class="aak-bottom-nav-more-sheet-handle"></div>';

    MORE_LINKS.forEach(function (item) {
      var link = document.createElement("a");
      link.href = item.href;
      link.innerHTML =
        '<span aria-hidden="true">' + item.icon + "</span><span>" + item.label + "</span>";
      panel.appendChild(link);
    });

    sheet.appendChild(panel);

    sheet.addEventListener("click", function (event) {
      if (event.target === sheet) {
        sheet.classList.remove("show");
      }
    });

    var moreButton = document.createElement("a");
    moreButton.href = "#";
    moreButton.innerHTML =
      '<span class="aak-bottom-nav-icon" aria-hidden="true">☰</span><span>More</span>';

    moreButton.addEventListener("click", function (event) {
      event.preventDefault();
      sheet.classList.add("show");
    });

    nav.appendChild(moreButton);

    document.body.appendChild(nav);
    document.body.appendChild(sheet);
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

    buildThemeToggle();

    // The bottom nav links to protected pages - don't show it on the public login page.
    var isLoginPage = (window.location.pathname.replace(/\/$/, "") || "/") === "/login";

    if (!isLoginPage) {
      buildBottomNav();
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

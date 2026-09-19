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

  // Roles omitted entirely (no "roles" key) means every logged-in role can see the link.
  // These lists mirror the module access rules in SecurityConfig.java.
  var OFFICE_ONLY_ROLES = ["ADMIN", "OFFICE"];

  var BOTTOM_NAV_LINKS = [
    { href: "/", label: "Dashboard", icon: "🏠" },
    { href: "/customers", label: "Customers", icon: "👥", roles: ["ADMIN", "OFFICE", "SALES_REP"] },
    { href: "/sales-invoices", label: "Sales", icon: "🧾", roles: ["ADMIN", "OFFICE", "SALES_REP"] },
    { href: "/payments", label: "Payments", icon: "💳", roles: OFFICE_ONLY_ROLES }
  ];

  var MORE_LINKS = [
    { href: "/products", label: "Products", icon: "📦", roles: OFFICE_ONLY_ROLES },
    { href: "/purchase-invoices", label: "Purchase Invoices", icon: "📥", roles: OFFICE_ONLY_ROLES },
    { href: "/purchase-invoices/supplier-balance", label: "Supplier Balance", icon: "🏦", roles: OFFICE_ONLY_ROLES },
    { href: "/inventory", label: "Inventory", icon: "📊", roles: OFFICE_ONLY_ROLES },
    { href: "/inventory/adjustments", label: "Stock Adjustments", icon: "🛠️", roles: OFFICE_ONLY_ROLES },
    { href: "/customers/credit-followup", label: "Credit Follow-up", icon: "📋", roles: ["ADMIN", "OFFICE", "SALES_REP"] },
    { href: "/cheques", label: "Cheques", icon: "🏦", roles: OFFICE_ONLY_ROLES },
    { href: "/collections", label: "Collections", icon: "📬", roles: OFFICE_ONLY_ROLES },
    { href: "/collections/handover", label: "Daily Handover", icon: "🧾", roles: OFFICE_ONLY_ROLES },
    { href: "/reports", label: "Reports", icon: "📈", roles: OFFICE_ONLY_ROLES },
    { href: "/employees", label: "Employees", icon: "🧑‍💼", roles: OFFICE_ONLY_ROLES },
    { href: "/vehicles", label: "Vehicles", icon: "🚚", roles: OFFICE_ONLY_ROLES },
    { href: "/routes", label: "Routes", icon: "🗺️", roles: OFFICE_ONLY_ROLES },
    { href: "/delivery-trips", label: "Delivery Trips", icon: "🚛", roles: OFFICE_ONLY_ROLES },
    { href: "/delivery-trips/vehicle-stock", label: "Vehicle Stock", icon: "📦", roles: OFFICE_ONLY_ROLES },
    { href: "/shop-returns", label: "Shop Returns", icon: "↩️", roles: OFFICE_ONLY_ROLES },
    { href: "/supplier-returns", label: "CBL Returns", icon: "↩️", roles: OFFICE_ONLY_ROLES },
    { href: "/attendance", label: "Attendance", icon: "📅", roles: OFFICE_ONLY_ROLES },
    { href: "/advances", label: "Advances", icon: "💵", roles: OFFICE_ONLY_ROLES },
    { href: "/salary", label: "Salary", icon: "💰", roles: OFFICE_ONLY_ROLES },
    { href: "/users", label: "Manage Users", icon: "🔑", roles: ["ADMIN"] },
    { href: "/account/change-password", label: "Change Password", icon: "🔒" },
    { href: "/logout", label: "Logout", icon: "🚪" }
  ];

  function allowedForRole(item, role) {
    return !item.roles || item.roles.indexOf(role) !== -1;
  }

  function fetchCurrentRole(callback) {
    if (typeof window.fetch !== "function") {
      callback(null);
      return;
    }

    window.fetch("/account/role", { credentials: "same-origin" })
      .then(function (response) {
        return response.ok ? response.text() : "";
      })
      .then(function (role) {
        callback(role || null);
      })
      .catch(function () {
        callback(null);
      });
  }



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

  function buildBottomNav(role) {
    if (document.querySelector(".aak-bottom-nav")) {
      return;
    }

    document.body.classList.add("aak-has-bottom-nav");

    var visibleBottomLinks = BOTTOM_NAV_LINKS.filter(function (item) {
      return allowedForRole(item, role);
    });

    var visibleMoreLinks = MORE_LINKS.filter(function (item) {
      return allowedForRole(item, role);
    });

    var nav = document.createElement("nav");
    nav.className = "aak-bottom-nav";
    nav.setAttribute("aria-label", "Primary");

    visibleBottomLinks.forEach(function (item) {
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

    visibleMoreLinks.forEach(function (item) {
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
      fetchCurrentRole(function (role) {
        buildBottomNav(role);
      });
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

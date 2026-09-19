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
  // These mirror the module access rules in SecurityConfig.java, and are the single
  // source of truth for the desktop side nav, the mobile bottom nav and its "More" sheet.
  var OFFICE_ONLY_ROLES = ["ADMIN", "OFFICE"];
  var SHOP_AND_SALES_ROLES = ["ADMIN", "OFFICE", "SALES_REP"];

  var SIDEBAR_LINKS = [
    { href: "/", label: "Dashboard", icon: "🏠", bottomPrimary: true },
    { href: "/customers", label: "Customers", icon: "👥", roles: SHOP_AND_SALES_ROLES, bottomPrimary: true },
    { href: "/customers/credit-followup", label: "Credit Follow-up", icon: "📋", roles: SHOP_AND_SALES_ROLES },
    { href: "/products", label: "Products", icon: "📦", roles: OFFICE_ONLY_ROLES },
    { href: "/purchase-invoices", label: "Purchase Invoices", icon: "📥", roles: OFFICE_ONLY_ROLES },
    { href: "/purchase-invoices/supplier-balance", label: "Supplier Balance", icon: "🏦", roles: OFFICE_ONLY_ROLES },
    { href: "/sales-invoices", label: "Sales Invoices", icon: "🧾", shortLabel: "Sales", roles: SHOP_AND_SALES_ROLES, bottomPrimary: true },
    { href: "/inventory", label: "Inventory", icon: "📊", roles: OFFICE_ONLY_ROLES },
    { href: "/inventory/adjustments", label: "Stock Adjustments", icon: "🛠️", roles: OFFICE_ONLY_ROLES },
    { href: "/payments", label: "Payments", icon: "💳", roles: OFFICE_ONLY_ROLES, bottomPrimary: true },
    { href: "/collections", label: "Collections", icon: "📬", roles: OFFICE_ONLY_ROLES },
    { href: "/collections/handover", label: "Daily Handover", icon: "🧾", roles: OFFICE_ONLY_ROLES },
    { href: "/cheques", label: "Cheques", icon: "🏦", roles: OFFICE_ONLY_ROLES },
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
    { href: "/users", label: "Manage Users", icon: "🔑", roles: ["ADMIN"] }
  ];

  var SIDEBAR_FOOTER_LINKS = [
    { href: "/account/change-password", label: "Change Password", icon: "🔒" },
    { href: "/logout", label: "Logout", icon: "🚪" }
  ];

  var BOTTOM_NAV_LINKS = SIDEBAR_LINKS.filter(function (item) {
    return item.bottomPrimary;
  });

  var MORE_LINKS = SIDEBAR_LINKS.filter(function (item) {
    return !item.bottomPrimary;
  }).concat(SIDEBAR_FOOTER_LINKS);

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

  // The JS-built nav has no server-rendered <form>, so Logout needs its own CSRF-safe
  // POST submission (the app's /logout endpoint, like the rest of the app, is POST-only).
  function performLogout() {
    if (!window.confirm("Are you sure you want to log out?")) {
      return;
    }

    var finish = function (csrf) {
      var form = document.createElement("form");
      form.method = "post";
      form.action = "/logout";

      if (csrf && csrf.parameterName && csrf.token) {
        var input = document.createElement("input");
        input.type = "hidden";
        input.name = csrf.parameterName;
        input.value = csrf.token;
        form.appendChild(input);
      }

      document.body.appendChild(form);
      form.submit();
    };

    if (typeof window.fetch !== "function") {
      finish(null);
      return;
    }

    window.fetch("/account/csrf", { credentials: "same-origin" })
      .then(function (response) {
        return response.ok ? response.json() : null;
      })
      .then(finish)
      .catch(function () {
        finish(null);
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
        "<span>" + (item.shortLabel || item.label) + "</span>";

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

      if (item.href === "/logout") {
        link.addEventListener("click", function (event) {
          event.preventDefault();
          performLogout();
        });
      }

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

  // Every page except home.html has no built-in navigation at all (just a "Back to
  // Dashboard" link) - this injects the same role-aware nav as a fixed sidebar so
  // every screen has real navigation, not just the dashboard.
  function buildSideNav(role) {
    if (document.querySelector(".sidebar") || document.querySelector(".aak-side-nav")) {
      return;
    }

    document.body.classList.add("aak-has-side-nav");

    var nav = document.createElement("nav");
    nav.className = "aak-side-nav";
    nav.setAttribute("aria-label", "Main menu");

    var brand = document.createElement("a");
    brand.className = "aak-side-nav-brand";
    brand.href = "/";
    brand.innerHTML =
      '<img src="/img/aak-logo-mark.svg" alt="AAK Agency"><span>AAK Agency</span>';
    nav.appendChild(brand);

    var links = document.createElement("div");
    links.className = "aak-side-nav-links";

    SIDEBAR_LINKS.filter(function (item) {
      return allowedForRole(item, role);
    }).forEach(function (item) {
      var link = document.createElement("a");
      link.href = item.href;

      if (isActivePath(item.href)) {
        link.classList.add("active");
      }

      link.innerHTML =
        '<span aria-hidden="true">' + item.icon + "</span><span>" + item.label + "</span>";

      links.appendChild(link);
    });

    nav.appendChild(links);

    var footer = document.createElement("div");
    footer.className = "aak-side-nav-footer";

    SIDEBAR_FOOTER_LINKS.forEach(function (item) {
      var link = document.createElement("a");
      link.href = item.href;
      link.innerHTML =
        '<span aria-hidden="true">' + item.icon + "</span><span>" + item.label + "</span>";

      if (item.href === "/logout") {
        link.addEventListener("click", function (event) {
          event.preventDefault();
          performLogout();
        });
      }

      footer.appendChild(link);
    });

    nav.appendChild(footer);

    document.body.prepend(nav);
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
        buildSideNav(role);
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

(function initAppShellModule() {
  function prefersReducedMotion() {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  function injectBackdrop() {
    if (document.querySelector('.app-backdrop')) {
      return;
    }

    const backdrop = document.createElement('div');
    backdrop.className = 'app-backdrop';

    const grid = document.createElement('div');
    grid.className = 'app-grid';

    const aurora = document.createElement('div');
    aurora.className = 'app-aurora';

    const ring = document.createElement('div');
    ring.className = 'app-ring';

    const particles = document.createElement('div');
    particles.className = 'app-particles';

    const particleCount = prefersReducedMotion() ? 0 : 6;
    for (let index = 0; index < particleCount; index += 1) {
      const particle = document.createElement('span');
      particle.className = 'app-particle';
      particle.style.left = `${Math.random() * 100}%`;
      particle.style.top = `${Math.random() * 100}%`;
      particle.style.setProperty('--size', `${6 + Math.random() * 8}px`);
      particle.style.setProperty('--duration', `${10 + Math.random() * 6}s`);
      particle.style.setProperty('--delay', `${Math.random() * 6}s`);
      particles.appendChild(particle);
    }

    backdrop.append(grid, aurora, ring, particles);
    document.body.prepend(backdrop);
  }

  function injectProgressBar() {
    if (document.querySelector('.app-progress')) {
      return;
    }

    const progress = document.createElement('div');
    progress.className = 'app-progress';
    document.body.prepend(progress);

    document.querySelectorAll('a[href]').forEach((anchor) => {
      const href = anchor.getAttribute('href');
      if (!href || href.startsWith('#') || href.startsWith('javascript:') || href.startsWith('http')) {
        return;
      }

      anchor.addEventListener('click', () => {
        progress.classList.remove('is-active');
        requestAnimationFrame(() => progress.classList.add('is-active'));
      });
    });
  }

  function initReveal() {
    const candidates = [
      '.glass-panel',
      '.glass',
      '.panel',
      '#detailContent',
      '#historyList',
      'table',
      'form',
      'h1',
      'h2',
      '.voice-summary-card',
      '.voice-card'
    ];

    const nodes = Array.from(new Set(candidates.flatMap((selector) => Array.from(document.querySelectorAll(selector)))));
    if (!nodes.length) {
      return;
    }

    let observer;

    function revealNode(node) {
      node.classList.add('reveal-in');
      observer?.unobserve(node);
    }

    function isInInitialViewport(node) {
      const rect = node.getBoundingClientRect();
      const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
      const viewportWidth = window.innerWidth || document.documentElement.clientWidth;

      if (rect.width <= 0 || rect.height <= 0) {
        return false;
      }

      return rect.bottom > 0 && rect.right > 0 && rect.top < viewportHeight && rect.left < viewportWidth;
    }

    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          revealNode(entry.target);
        }
      });
    }, {
      threshold: 0.01,
      rootMargin: '0px 0px -8% 0px'
    });

    nodes.forEach((node, index) => {
      node.classList.add('reveal-enter');
      node.style.transitionDelay = `${Math.min(index * 30, 180)}ms`;
      observer.observe(node);
    });

    requestAnimationFrame(() => {
      nodes.forEach((node) => {
        if (isInInitialViewport(node)) {
          revealNode(node);
        }
      });
    });
  }

  function initShell() {
    document.body.dataset.page = window.location.hash || window.location.pathname || '/';
    injectBackdrop();
    injectProgressBar();
    initReveal();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initShell, { once: true });
  } else {
    initShell();
  }
})();

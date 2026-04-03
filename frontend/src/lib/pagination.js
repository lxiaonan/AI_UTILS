export const PAGE_SIZE_OPTIONS = [5, 10, 20];

export function clampPage(page, totalPages) {
  const safeTotal = Math.max(totalPages || 1, 1);
  const nextPage = Number(page) || 1;
  return Math.min(Math.max(nextPage, 1), safeTotal);
}

export function slicePage(items, page, pageSize) {
  const safePageSize = Math.max(pageSize || 1, 1);
  const safePage = Math.max(page || 1, 1);
  const start = (safePage - 1) * safePageSize;
  return items.slice(start, start + safePageSize);
}

export function buildPagination(currentPage, totalPages, windowSize = 5) {
  const safeTotal = Math.max(totalPages || 1, 1);
  const safeCurrent = clampPage(currentPage, safeTotal);
  const safeWindow = Math.max(windowSize || 1, 1);
  const pages = [];

  let start = Math.max(1, safeCurrent - Math.floor(safeWindow / 2));
  let end = Math.min(safeTotal, start + safeWindow - 1);

  start = Math.max(1, end - safeWindow + 1);

  if (start > 1) {
    pages.push(1);
  }
  if (start > 2) {
    pages.push('ellipsis-start');
  }
  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }
  if (end < safeTotal - 1) {
    pages.push('ellipsis-end');
  }
  if (end < safeTotal) {
    pages.push(safeTotal);
  }

  return pages;
}

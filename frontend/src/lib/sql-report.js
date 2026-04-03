function parseMarkdown(text) {
  if (!text) {
    return '';
  }
  if (window.marked && typeof window.marked.parse === 'function') {
    return window.marked.parse(text);
  }
  return text;
}

export function extractSection(text, keyword) {
  const source = String(text || '');
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(`【${escapedKeyword}】([\\s\\S]*?)(?=【|$)`, 'i');
  const match = source.match(regex);
  return match ? match[1].trim() : '';
}

export function buildSqlAnalysis(aiText) {
  const sourceText = String(aiText || '');
  const prefixMatch = sourceText.match(/^([\s\S]*?)(?=【|$)/);
  const reasoningText = prefixMatch && prefixMatch[1].trim() ? prefixMatch[1].trim() : '';
  const scoreText = extractSection(sourceText, 'SQL评分');
  const analysisText = extractSection(sourceText, '问题分析');
  const suggestText = extractSection(sourceText, '优化建议');
  const sqlText = extractSection(sourceText, '优化后SQL');
  const scoreMatch = sourceText.match(/总分[：:]\s*(\d+)/);

  return {
    score: scoreMatch ? scoreMatch[1] : '--',
    isModular: Boolean(scoreText || analysisText || suggestText || sqlText),
    reasoningHtml: parseMarkdown(reasoningText),
    scoreHtml: parseMarkdown(scoreText),
    analysisHtml: parseMarkdown(analysisText),
    suggestHtml: parseMarkdown(suggestText),
    sqlHtml: parseMarkdown(sqlText),
    sqlRaw: sqlText || '',
    fallbackHtml: parseMarkdown(sourceText)
  };
}

export async function copyPlainText(text) {
  const value = String(text || '');
  if (!value) {
    return false;
  }

  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(value);
    return true;
  }

  const textArea = document.createElement('textarea');
  textArea.value = value;
  textArea.style.position = 'fixed';
  textArea.style.top = '0';
  textArea.style.left = '0';
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  document.execCommand('copy');
  document.body.removeChild(textArea);
  return true;
}

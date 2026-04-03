<template>
  <AppShellLayout>
    <div class="max-w-7xl mx-auto px-4 pb-12 w-full">
      <div class="mb-10 text-center">
        <h2 class="text-5xl font-extrabold gradient-text tracking-tight mb-4">智能语音工坊</h2>
        <p class="text-slate-400 text-lg font-medium tracking-wide">多语言、多音色文本转语音生成。</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 glass-panel rounded-3xl p-8 h-fit border border-slate-700/50">
          <div class="flex justify-between items-center mb-6">
            <label class="text-xl font-bold text-slate-200">输入文本</label>
            <span class="text-xs bg-slate-800 text-slate-400 px-3 py-1.5 rounded-full border border-slate-700 font-mono">模型：qwen3-tts-flash</span>
          </div>

          <div class="mb-5">
            <div class="flex items-center justify-between mb-3">
              <div class="text-sm font-bold text-slate-300">语言选择</div>
              <div class="text-xs text-slate-500">官方多语种音色表</div>
            </div>
            <div class="flex flex-wrap gap-3">
              <button
                v-for="language in supportedLanguages"
                :key="language"
                type="button"
                class="voice-chip"
                :class="{ active: language === form.languageType }"
                @click="selectLanguage(language)"
              >
                {{ formatLanguageLabel(language) }}
              </button>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-5">
            <div class="voice-summary-card">
              <div class="text-xs uppercase tracking-[0.24em] text-slate-500 mb-2">已选音色</div>
              <div class="text-xl font-bold text-slate-100 mb-2">{{ selectedVoiceMeta.code }} · {{ selectedVoiceMeta.displayName }}</div>
              <div class="text-sm text-slate-400">{{ selectedVoiceMeta.title }}. {{ selectedVoiceMeta.note }}</div>
            </div>
            <div class="voice-summary-card">
              <div class="text-xs uppercase tracking-[0.24em] text-slate-500 mb-2">语言与风格</div>
              <div class="text-xl font-bold text-slate-100 mb-2">{{ formatLanguageLabel(form.languageType) }}</div>
              <div class="text-sm text-slate-400">{{ voiceHint }}</div>
            </div>
          </div>

          <div class="space-y-5 mb-6">
            <div>
              <div class="voice-section-title text-slate-100 font-bold">推荐音色</div>
              <div class="voice-grid">
                <button
                  v-for="item in recommendedVoices"
                  :key="`spotlight-${item.code}`"
                  type="button"
                  class="voice-card"
                  :class="{ active: item.code === form.voiceCode, 'is-recommended': item.recommended.includes(form.languageType) }"
                  @click="selectVoice(item.code)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <div class="text-lg font-bold text-slate-100">{{ getVoiceMeta(item.code).code }}</div>
                      <div class="text-sm text-slate-400">{{ getVoiceMeta(item.code).displayName }} · {{ getVoiceMeta(item.code).title }}</div>
                    </div>
                    <span class="voice-badge">{{ getVoiceMeta(item.code).code }}</span>
                  </div>
                  <div class="text-sm text-slate-300 leading-relaxed">{{ getVoiceMeta(item.code).note }}</div>
                  <div class="flex flex-wrap gap-2">
                    <span class="voice-badge">{{ groupTitle(getVoiceMeta(item.code).group) }}</span>
                    <span v-if="item.recommended.includes(form.languageType)" class="voice-badge">当前推荐</span>
                  </div>
                </button>
              </div>
            </div>

            <div class="space-y-6">
              <section v-for="group in groupedVoiceSections" :key="group.id">
                <div class="voice-section-title text-slate-100 font-bold">{{ group.title }}</div>
                <p class="text-sm text-slate-500 mb-4">{{ group.description }}</p>
                <div class="voice-grid">
                  <button
                    v-for="item in group.voices"
                    :key="`${group.id}-${item.code}`"
                    type="button"
                    class="voice-card"
                    :class="{ active: item.code === form.voiceCode, 'is-recommended': item.recommended.includes(form.languageType) }"
                    @click="selectVoice(item.code)"
                  >
                    <div class="flex items-start justify-between gap-3">
                      <div>
                        <div class="text-lg font-bold text-slate-100">{{ getVoiceMeta(item.code).code }}</div>
                        <div class="text-sm text-slate-400">{{ getVoiceMeta(item.code).displayName }} · {{ getVoiceMeta(item.code).title }}</div>
                      </div>
                      <span class="voice-badge">{{ getVoiceMeta(item.code).code }}</span>
                    </div>
                    <div class="text-sm text-slate-300 leading-relaxed">{{ getVoiceMeta(item.code).note }}</div>
                    <div class="flex flex-wrap gap-2">
                      <span class="voice-badge">{{ group.title }}</span>
                      <span v-if="item.recommended.includes(form.languageType)" class="voice-badge">当前推荐</span>
                    </div>
                  </button>
                </div>
              </section>
            </div>
          </div>

          <textarea
            v-model.trim="form.text"
            rows="6"
            class="w-full bg-slate-900/60 border border-slate-700 rounded-2xl p-5 text-slate-200 text-lg leading-relaxed resize-none transition-all placeholder-slate-600 outline-none neon-border"
            placeholder="请输入想要转成语音的文本内容。"
          />

          <div v-if="errorMessage" class="mt-4 text-sm text-rose-300 bg-rose-900/30 border border-rose-800 rounded-xl p-3">{{ errorMessage }}</div>

          <button
            class="mt-6 w-full bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl transition-all btn-glow flex justify-center items-center text-lg"
            :class="{ 'opacity-70 cursor-not-allowed': loading }"
            :disabled="loading"
            @click="generateVoice"
          >
            {{ loading ? '生成中...' : '开始生成语音' }}
          </button>

          <div class="mt-8 flex flex-col items-center bg-slate-900/40 p-6 rounded-2xl border border-slate-700/50 min-h-[180px]">
            <div v-if="resultState === 'loading'" class="text-sky-300 font-bold">AI 正在合成语音...</div>
            <div v-else-if="resultState === 'audio'" class="w-full flex justify-center mt-2">
              <div class="w-full bg-white rounded-2xl p-2 border border-blue-100 flex justify-center">
                <audio controls :src="resultAudioUrl" class="w-full outline-none" autoplay />
              </div>
            </div>
            <div v-else-if="resultState === 'error'" class="w-full bg-rose-900/30 text-rose-300 px-4 py-3 rounded-lg text-center text-sm border border-rose-800">{{ errorMessage }}</div>
            <div v-else class="text-center text-slate-500">生成的音频将在这里播放。</div>
          </div>
        </div>

        <div class="glass-panel rounded-3xl p-6 flex flex-col h-[600px] border border-slate-700/50 relative">
          <h3 class="text-xl font-bold text-slate-200 mb-6 border-b border-slate-700/50 pb-4">语音档案库</h3>
          <div class="flex-grow overflow-y-auto pr-2 space-y-4">
            <div v-if="historyLoading" class="text-center text-slate-500 mt-10">加载历史中...</div>
            <div v-else-if="!history.length" class="text-center text-slate-500 mt-10 text-sm">暂无生成记录。</div>
            <div v-for="item in history" :key="item.id" class="bg-slate-800/50 p-5 rounded-2xl border border-slate-700 hover:border-sky-500/50 transition-colors">
              <div class="flex justify-between items-start mb-3">
                <span class="text-xs text-slate-400">{{ formatHistoryDate(item.createTime) }}</span>
                <div class="flex items-center gap-2 ml-3">
                  <span class="text-[11px] text-sky-300 bg-sky-900/30 px-2 py-1 rounded-full border border-sky-500/20">{{ formatLanguageLabel(item.languageType || 'Chinese') }}</span>
                  <span class="text-[11px] text-cyan-300 bg-cyan-900/30 px-2 py-1 rounded-full border border-cyan-500/20">{{ formatVoiceLabel(item.voiceCode || 'Cherry') }}</span>
                </div>
              </div>
              <p class="text-sm text-slate-300 font-medium mb-4 line-clamp-2" :title="item.userText">"{{ item.userText }}"</p>
              <audio v-if="item.audioUrl" controls :src="item.audioUrl" class="w-full h-10 outline-none rounded-lg" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api } from '@/lib/api';
import { consumePendingTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const SUPPORTED_LANGUAGES = ['Chinese', 'Cantonese', 'English', 'French', 'German', 'Russian', 'Italian', 'Spanish', 'Portuguese', 'Japanese', 'Korean'];
const LANGUAGE_LABELS = {
  Chinese: '中文',
  Cantonese: '粤语',
  English: '英文',
  French: '法语',
  German: '德语',
  Russian: '俄语',
  Italian: '意大利语',
  Spanish: '西班牙语',
  Portuguese: '葡萄牙语',
  Japanese: '日语',
  Korean: '韩语'
};
const VOICE_CATALOG = [
  { code: 'Cherry', label: 'Bright female', note: '适合中文与中英混读的通用女声。', recommended: ['Chinese', 'English'] },
  { code: 'Serena', label: 'Gentle female', note: '柔和女声，适合解说和旁白。', recommended: ['Chinese', 'English'] },
  { code: 'Ethan', label: 'Steady male', note: '稳重男声，适合中性表达。', recommended: ['Chinese', 'English'] },
  { code: 'Chelsie', label: 'Clear female', note: '清晰有力的明亮女声。', recommended: ['Chinese', 'English'] },
  { code: 'Dylan', label: 'Beijing male', note: '偏北京口音的男声。', recommended: ['Chinese'] },
  { code: 'Jada', label: 'Shanghai female', note: '偏上海语感的女声。', recommended: ['Chinese'] },
  { code: 'Sunny', label: 'Taiwanese female', note: '偏台式普通话的女声。', recommended: ['Chinese'] },
  { code: 'Nofish', label: 'Relaxed male', note: '轻松自然的男声。', recommended: ['Chinese', 'English'] },
  { code: 'Jennifer', label: 'English female', note: '适合英文内容的自然女声。', recommended: ['English'] },
  { code: 'Marcus', label: 'Northeast male', note: '东北风格男声。', recommended: ['Chinese'] },
  { code: 'Roy', label: 'Warm male', note: '成熟温和的男声。', recommended: ['Chinese', 'English'] },
  { code: 'Peter', label: 'Story male', note: '适合讲故事的男声。', recommended: ['Chinese', 'English'] },
  { code: 'Eric', label: 'Spoken male', note: '口语化表达更自然。', recommended: ['Chinese', 'English'] },
  { code: 'Rocky', label: 'Power male', note: '更有力度的粤语男声。', recommended: ['Cantonese'] },
  { code: 'Kiki', label: 'Cantonese female', note: '年轻化粤语女声。', recommended: ['Cantonese'] },
  { code: 'Ryan', label: 'Magnetic male', note: '磁性男声，适合广告。', recommended: ['Chinese', 'English'] },
  { code: 'Katerina', label: 'Soft female', note: '顺滑温柔的女声。', recommended: ['Chinese', 'English'] },
  { code: 'Elias', label: 'Documentary male', note: '适合纪录片和正式旁白。', recommended: ['Chinese', 'English'] },
  { code: 'Momo', label: 'Playful female', note: '活泼女声。', recommended: ['Chinese', 'Japanese', 'Korean'] },
  { code: 'Vivian', label: 'Elegant female', note: '优雅叙述风格。', recommended: ['Chinese', 'English', 'French'] },
  { code: 'Aiden', label: 'English male', note: '自然英文男声。', recommended: ['English'] },
  { code: 'Sohee', label: 'Korean female', note: '适合韩语内容。', recommended: ['Korean'] },
  { code: 'Sonrisa', label: 'Spanish female', note: '适合西语内容。', recommended: ['Spanish'] },
  { code: 'Dolce', label: 'Italian female', note: '适合意语内容。', recommended: ['Italian'] },
  { code: 'Ono Anna', label: 'Japanese female', note: '适合日语内容。', recommended: ['Japanese'] },
  { code: 'Lenn', label: 'German male', note: '适合德语内容。', recommended: ['German'] },
  { code: 'Emilien', label: 'French male', note: '适合法语内容。', recommended: ['French'] }
];
const VOICE_GROUPS = [
  { id: 'global', title: '全球多语种音色', description: '适合长文本朗读、解说与通用场景。' },
  { id: 'character', title: '角色化与风格化音色', description: '更有角色感与表演感。' },
  { id: 'regional', title: '地域与方言音色', description: '适合中文口音与本地化表达。' }
];
const OFFICIAL_META = {
  Cherry: { displayName: 'Qianyue', title: 'Sunny narrator', group: 'global' },
  Serena: { displayName: 'Suyao', title: 'Gentle narrator', group: 'global' },
  Ethan: { displayName: 'Chenxu', title: 'Warm male host', group: 'global' },
  Chelsie: { displayName: 'Qianxue', title: 'Virtual partner', group: 'character' },
  Momo: { displayName: 'Motu', title: 'Playful teaser', group: 'character' },
  Vivian: { displayName: 'Shisan', title: 'Cool rebel', group: 'character' },
  Marcus: { displayName: 'Qinchuan', title: 'Regional male', group: 'regional' },
  Roy: { displayName: 'Ajie', title: 'Regional male', group: 'regional' },
  Peter: { displayName: 'Lipeter', title: 'Story teller', group: 'regional' },
  Sunny: { displayName: 'Qinger', title: 'Sweet regional', group: 'regional' },
  Eric: { displayName: 'Chengchuan', title: 'City male', group: 'regional' },
  Rocky: { displayName: 'Aqiang', title: 'Cantonese talker', group: 'regional' }
};

function getVoiceMeta(voiceCode) {
  const base = VOICE_CATALOG.find((item) => item.code === voiceCode) || VOICE_CATALOG[0];
  const official = OFFICIAL_META[base.code] || {};
  return {
    ...base,
    displayName: official.displayName || base.code,
    title: official.title || base.label,
    group: official.group || 'global'
  };
}

function formatLanguageLabel(languageType) {
  return LANGUAGE_LABELS[languageType] || languageType || '中文';
}

function formatVoiceLabel(voiceCode) {
  const meta = getVoiceMeta(voiceCode);
  return `${meta.code} · ${meta.displayName}`;
}

function getRecommendedVoices(languageType) {
  const voices = VOICE_CATALOG.filter((item) => item.recommended.includes(languageType));
  return voices.length > 0 ? voices : VOICE_CATALOG.slice(0, 4);
}

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  text: '',
  languageType: 'Chinese',
  voiceCode: 'Cherry'
});
const loading = ref(false);
const historyLoading = ref(true);
const errorMessage = ref('');
const resultState = ref('empty');
const resultAudioUrl = ref('');
const history = ref([]);

const supportedLanguages = SUPPORTED_LANGUAGES;
const voiceGroups = VOICE_GROUPS;

const selectedVoiceMeta = computed(() => getVoiceMeta(form.voiceCode));
const voiceHint = computed(() => `${selectedVoiceMeta.value.code} 属于“${groupTitle(selectedVoiceMeta.value.group)}”，当前按 ${formatLanguageLabel(form.languageType)} 优先推荐。`);
const recommendedVoices = computed(() => getRecommendedVoices(form.languageType));
const groupedVoiceSections = computed(() => voiceGroups
  .map((group) => ({
    ...group,
    voices: VOICE_CATALOG.filter((item) =>
      (OFFICIAL_META[item.code]?.group || 'global') === group.id &&
      item.recommended.includes(form.languageType)
    )
  }))
  .filter((group) => group.voices.length > 0));

function groupTitle(groupId) {
  return voiceGroups.find((group) => group.id === groupId)?.title || '官方音色';
}

function selectLanguage(languageType) {
  form.languageType = languageType;
  const nextRecommended = getRecommendedVoices(languageType)[0];
  if (nextRecommended) {
    form.voiceCode = nextRecommended.code;
  }
}

function selectVoice(voiceCode) {
  form.voiceCode = voiceCode;
}

function applyPendingVoiceTemplate() {
  const pending = consumePendingTemplate('voice');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.text === 'string') {
    form.text = fields.text;
  }
  if (typeof fields.languageType === 'string' && fields.languageType) {
    selectLanguage(fields.languageType);
  }
  if (typeof fields.voiceCode === 'string' && fields.voiceCode) {
    form.voiceCode = fields.voiceCode;
  }
}

function formatHistoryDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

function extractAudioUrl(data) {
  if (data.output?.audio?.url) {
    return data.output.audio.url;
  }
  const contents = data.output?.choices?.[0]?.message?.content;
  if (Array.isArray(contents)) {
    const audioItem = contents.find((item) => item.audio);
    return audioItem?.audio || '';
  }
  return '';
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const { response, data } = await api.getVoiceHistory();
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    history.value = Array.isArray(data) ? data : [];
  } catch (error) {
    history.value = [];
  } finally {
    historyLoading.value = false;
  }
}

async function generateVoice() {
  const text = form.text.trim();
  if (!text) {
    errorMessage.value = '请输入需要合成的文本。';
    resultState.value = 'error';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  resultState.value = 'loading';
  resultAudioUrl.value = '';

  try {
    const { response, data } = await api.generateVoice({
      text,
      languageType: form.languageType,
      voice: form.voiceCode
    });
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (data?.code && data?.message) {
      throw new Error(`${data.code}: ${data.message}`);
    }
    const audioUrl = extractAudioUrl(data);
    if (!audioUrl) {
      throw new Error('未能从返回结果中提取到音频链接');
    }
    resultAudioUrl.value = audioUrl;
    resultState.value = 'audio';
    await loadHistory();
    await auth.fetchCurrentUser();
  } catch (error) {
    errorMessage.value = error.message;
    resultState.value = 'error';
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  applyPendingVoiceTemplate();
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadHistory();
  }
});
</script>

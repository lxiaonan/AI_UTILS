<template>
  <AppShellLayout>
    <div class="max-w-7xl mx-auto px-4 pb-12 w-full">
      <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50 mb-8">
        <div class="flex items-start justify-between gap-6 flex-wrap">
          <div class="max-w-3xl">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Video Studio</div>
            <h1 class="mt-3 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight">视频生成工作台</h1>
            <p class="mt-4 text-slate-300 leading-8">
              图生视频改成直接上传图片和音频，视频模仿也改成上传参考视频。上传完成后，页面会自动把文件转换为可提交的资源地址。
            </p>
          </div>
          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">当前模型</div>
              <div class="studio-metric-value">{{ currentModel.label }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">任务状态</div>
              <div class="studio-metric-value">{{ formatStatus(activeTaskStatus) }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">历史记录</div>
              <div class="studio-metric-value">{{ history.length }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div class="lg:col-span-8 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
            <div class="grid gap-5 md:grid-cols-2">
              <div class="md:col-span-2">
                <label class="block text-sm font-bold text-slate-300 mb-3">模型选择</label>
                <select
                  v-model="form.model"
                  class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border"
                  @change="applyModelDefaults(form.model)"
                >
                  <option v-for="item in MODEL_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
                <div class="mt-2 text-sm text-slate-500">{{ currentModel.help }}</div>
              </div>

              <div class="md:col-span-2">
                <label class="block text-sm font-bold text-slate-300 mb-3">视频提示词</label>
                <textarea
                  v-model.trim="form.prompt"
                  rows="6"
                  class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.6rem] p-5 text-slate-100 leading-7 resize-none outline-none neon-border placeholder-slate-500"
                  placeholder="描述主体、动作、场景、镜头氛围与风格。"
                ></textarea>
              </div>

              <template v-if="currentModel.mode === 'i2v'">
                <div>
                  <div class="block text-sm font-bold text-slate-300 mb-3">参考图片</div>
                  <input ref="imageInput" type="file" accept=".png,.jpg,.jpeg,.bmp,.webp,image/png,image/jpeg,image/webp,image/bmp" class="hidden" @change="handleImageChange">
                  <div v-if="!imageUpload.preview" class="studio-upload-placeholder cursor-pointer min-h-[260px]" @click="triggerImageSelect">
                    <div class="text-sm font-bold text-slate-100">上传图生视频图片</div>
                    <div class="mt-2 text-sm text-slate-400">支持 PNG / JPG / JPEG / BMP / WEBP</div>
                    <div class="mt-4 inline-flex items-center rounded-full border border-sky-500/30 px-4 py-2 text-sky-300">选择图片</div>
                  </div>
                  <div v-else class="studio-thumb rounded-[1.5rem] overflow-hidden">
                    <img :src="imageUpload.preview" class="w-full h-64 object-cover" alt="参考图片">
                    <div class="p-4 flex items-center justify-between gap-3 flex-wrap">
                      <div>
                        <div class="text-sm text-slate-100 font-semibold break-all">{{ imageUpload.name }}</div>
                        <div class="mt-1 text-xs" :class="imageUpload.error ? 'text-rose-300' : 'text-slate-400'">
                          {{ imageUpload.error || imageUpload.status || '图片已上传' }}
                        </div>
                      </div>
                      <div class="flex gap-2">
                        <button type="button" class="px-4 py-2 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="triggerImageSelect">重新上传</button>
                        <button type="button" class="px-4 py-2 rounded-xl bg-rose-900/20 text-rose-200 border border-rose-500/30 hover:bg-rose-900/35 transition" @click="clearUpload(imageUpload, imageInput)">清空</button>
                      </div>
                    </div>
                  </div>
                </div>

                <div>
                  <div class="block text-sm font-bold text-slate-300 mb-3">参考音频</div>
                  <input ref="audioInput" type="file" accept=".mp3,.wav,audio/mpeg,audio/wav,audio/x-wav" class="hidden" @change="handleAudioChange">
                  <div v-if="!audioUpload.preview" class="studio-upload-placeholder cursor-pointer min-h-[260px]" @click="triggerAudioSelect">
                    <div class="text-sm font-bold text-slate-100">上传音频文件</div>
                    <div class="mt-2 text-sm text-slate-400">支持 MP3 / WAV，可不上传</div>
                    <div class="mt-4 inline-flex items-center rounded-full border border-sky-500/30 px-4 py-2 text-sky-300">选择音频</div>
                  </div>
                  <div v-else class="studio-thumb rounded-[1.5rem] overflow-hidden p-5 space-y-4">
                    <div>
                      <div class="text-sm text-slate-100 font-semibold break-all">{{ audioUpload.name }}</div>
                      <div class="mt-1 text-xs" :class="audioUpload.error ? 'text-rose-300' : 'text-slate-400'">
                        {{ audioUpload.error || audioUpload.status || '音频已上传' }}
                      </div>
                    </div>
                    <audio :src="audioUpload.preview" controls class="w-full outline-none"></audio>
                    <div class="flex gap-2 flex-wrap">
                      <button type="button" class="px-4 py-2 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="triggerAudioSelect">重新上传</button>
                      <button type="button" class="px-4 py-2 rounded-xl bg-rose-900/20 text-rose-200 border border-rose-500/30 hover:bg-rose-900/35 transition" @click="clearUpload(audioUpload, audioInput)">清空</button>
                    </div>
                  </div>
                </div>
              </template>

              <div v-if="currentModel.mode === 'r2v'" class="md:col-span-2">
                <div class="block text-sm font-bold text-slate-300 mb-3">参考视频</div>
                  <input ref="referenceVideoInput" type="file" accept=".mp4,.mov,video/mp4,video/quicktime" class="hidden" @change="handleReferenceVideoChange">
                <div v-if="!referenceVideoUpload.preview" class="studio-upload-placeholder cursor-pointer min-h-[300px]" @click="triggerReferenceVideoSelect">
                  <div class="text-sm font-bold text-slate-100">上传要模仿的视频</div>
                  <div class="mt-2 text-sm text-slate-400">支持 MP4 / MOV</div>
                  <div class="mt-4 inline-flex items-center rounded-full border border-sky-500/30 px-4 py-2 text-sky-300">选择视频</div>
                </div>
                <div v-else class="studio-thumb rounded-[1.5rem] overflow-hidden">
                  <video :src="referenceVideoUpload.preview" controls class="w-full max-h-[420px] bg-black"></video>
                  <div class="p-4 flex items-center justify-between gap-3 flex-wrap">
                    <div>
                      <div class="text-sm text-slate-100 font-semibold break-all">{{ referenceVideoUpload.name }}</div>
                      <div class="mt-1 text-xs" :class="referenceVideoUpload.error ? 'text-rose-300' : 'text-slate-400'">
                        {{ referenceVideoUpload.error || referenceVideoUpload.status || '参考视频已上传' }}
                      </div>
                    </div>
                    <div class="flex gap-2">
                      <button type="button" class="px-4 py-2 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="triggerReferenceVideoSelect">重新上传</button>
                      <button type="button" class="px-4 py-2 rounded-xl bg-rose-900/20 text-rose-200 border border-rose-500/30 hover:bg-rose-900/35 transition" @click="clearUpload(referenceVideoUpload, referenceVideoInput)">清空</button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="currentModel.mode === 'i2v'">
                <label class="block text-sm font-bold text-slate-300 mb-3">分辨率</label>
                <select v-model="form.resolution" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in RESOLUTION_OPTIONS" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div v-else>
                <label class="block text-sm font-bold text-slate-300 mb-3">画面尺寸</label>
                <select v-model="form.size" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in SIZE_OPTIONS" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">时长</label>
                <select v-model.number="form.duration" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in DURATION_OPTIONS" :key="item" :value="item">{{ item }} 秒</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">镜头类型</label>
                <select v-model="form.shotType" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option value="multi">multi</option>
                  <option value="single">single</option>
                </select>
              </div>

              <label v-if="supportsPromptExtend" class="rounded-[1.4rem] border border-slate-700/70 bg-slate-950/35 px-5 py-4 flex items-center justify-between gap-4">
                <span class="text-sm text-slate-200">自动扩写提示词</span>
                <input v-model="form.promptExtend" type="checkbox" class="h-5 w-5">
              </label>

              <label v-if="supportsAudio" class="rounded-[1.4rem] border border-slate-700/70 bg-slate-950/35 px-5 py-4 flex items-center justify-between gap-4">
                <span class="text-sm text-slate-200">输出音频</span>
                <input v-model="form.audio" type="checkbox" class="h-5 w-5">
              </label>
            </div>

            <div v-if="errorMessage && resultMode !== 'error'" class="mt-5 rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ errorMessage }}
            </div>

            <div class="mt-6 flex items-center justify-between gap-4 flex-wrap">
              <div class="text-sm text-slate-400">
                上传完成后会自动使用上传文件地址发起请求，不需要再手动填写 URL。
              </div>
              <button class="min-w-[220px] bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl transition-all btn-glow flex justify-center items-center text-lg" :class="{ 'opacity-60 cursor-not-allowed': loading || isAnyUploadBusy }" :disabled="loading || isAnyUploadBusy" @click="generateVideo">
                {{ loading ? '正在提交...' : '立即生成视频' }}
              </button>
            </div>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Result</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">任务结果</h2>
              </div>
              <div class="text-sm text-slate-400">{{ activeTaskId ? `任务 ID：${activeTaskId}` : '当前没有任务' }}</div>
            </div>

            <div class="studio-stage rounded-[1.75rem] p-4 md:p-6 min-h-[420px]">
              <div v-if="resultMode === 'submitting' || resultMode === 'polling'" class="h-full flex flex-col items-center justify-center text-center">
                <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                  <div class="w-7 h-7 rounded-full bg-sky-400/70" />
                </div>
                <div class="mt-5 text-lg font-bold text-slate-100">{{ resultMode === 'submitting' ? '正在创建任务...' : '任务执行中...' }}</div>
                <div class="mt-2 text-sm" :class="statusTextClass(activeTaskStatus)">{{ formatStatus(activeTaskStatus) }}</div>
                <button v-if="activeTaskId" type="button" class="mt-5 px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" :disabled="polling" @click="refreshCurrentTask">
                  {{ polling ? '刷新中...' : '立即刷新' }}
                </button>
              </div>

              <div v-else-if="resultMode === 'video'" class="space-y-5">
                <video :src="resultVideoUrl" controls class="w-full rounded-[1.25rem] border border-slate-700/60 bg-black max-h-[720px]"></video>
                <div class="flex gap-3 flex-wrap">
                  <button type="button" class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition" @click="openVideo(resultVideoUrl)">新窗口查看</button>
                  <button type="button" class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="downloadVideo(resultVideoUrl)">下载视频</button>
                </div>
              </div>

              <div v-else-if="resultMode === 'error'" class="rounded-[1.5rem] border border-rose-800 bg-rose-900/25 p-5 text-rose-200">
                <div class="font-bold">视频生成失败</div>
                <div class="mt-2">{{ errorMessage }}</div>
                <pre v-if="rawResponse" class="text-xs mt-3 whitespace-pre-wrap overflow-auto">{{ rawResponse }}</pre>
              </div>

              <div v-else-if="resultMode === 'raw'" class="rounded-[1.5rem] border border-yellow-800 bg-yellow-900/20 p-5 text-yellow-200">
                <div class="font-bold">任务已完成，但没有解析到视频地址</div>
                <pre class="text-xs mt-3 whitespace-pre-wrap overflow-auto">{{ rawResponse }}</pre>
              </div>

              <div v-else class="h-full flex items-center justify-center text-center text-slate-400">
                视频结果会显示在这里。
              </div>
            </div>
          </section>
        </div>

        <div class="lg:col-span-4">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50 h-[840px] flex flex-col">
            <div class="flex items-start justify-between gap-4 pb-5 border-b border-slate-700/60">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">History</div>
                <h2 class="mt-2 text-xl font-bold text-slate-100">视频历史</h2>
              </div>
              <div class="text-xs text-slate-500">{{ history.length }} 条</div>
            </div>

            <div class="flex-1 overflow-y-auto pr-1 mt-5 space-y-4">
              <div v-if="historyLoading" class="text-center text-slate-500 mt-12">正在加载历史...</div>
              <div v-else-if="!history.length" class="text-center text-slate-500 mt-12">还没有视频生成记录。</div>
              <article v-for="item in history" :key="item.id" class="studio-history-card">
                <div class="flex items-start justify-between gap-3">
                  <div class="text-xs text-slate-400">{{ formatDate(item.createTime) }}</div>
                  <span class="text-[11px] px-2 py-1 rounded-full border" :class="statusPillClass(item.taskStatus)">{{ formatStatus(item.taskStatus) }}</span>
                </div>
                <div class="mt-3 text-[11px] text-slate-500 uppercase tracking-[0.18em]">{{ item.aiModel }}</div>
                <div class="mt-3 text-sm text-slate-200 leading-7 break-words">{{ parsePrompt(item.userPrompt) }}</div>
                <div class="mt-2 text-xs text-slate-500">{{ parseSummary(item.userPrompt) }}</div>
                <div class="mt-4 flex gap-3 flex-wrap">
                  <button v-if="item.videoUrl" type="button" class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition" @click="loadHistoryItem(item)">载入结果</button>
                  <button v-if="item.taskId && !item.videoUrl && !isFailure(item.taskStatus)" type="button" class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition" @click="resumeTask(item.taskId)">继续查询</button>
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import { consumePendingTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const MODEL_OPTIONS = [
  { value: 'wan2.6-t2v', label: 'wan2.6-t2v 文生视频', mode: 't2v', help: '只需要提示词，可选输出音频。' },
  { value: 'wan2.6-i2v', label: 'wan2.6-i2v 图生视频', mode: 'i2v', help: '使用上传图片生成视频，音频文件可选上传。' },
  { value: 'wan2.6-r2v-flash', label: 'wan2.6-r2v-flash 视频模仿', mode: 'r2v', help: '上传参考视频后，按提示词做重生成。' }
];
const RESOLUTION_OPTIONS = ['720P', '1080P'];
const SIZE_OPTIONS = ['1280*720', '720*1280', '832*480', '480*832'];
const DURATION_OPTIONS = [5, 10];

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  model: 'wan2.6-t2v',
  prompt: '',
  resolution: '720P',
  size: '1280*720',
  duration: 10,
  shotType: 'multi',
  promptExtend: true,
  audio: true
});

const history = ref([]);
const historyLoading = ref(true);
const loading = ref(false);
const polling = ref(false);
const errorMessage = ref('');
const rawResponse = ref('');
const resultMode = ref('empty');
const resultVideoUrl = ref('');
const activeTaskId = ref('');
const activeTaskStatus = ref('');

const imageInput = ref(null);
const audioInput = ref(null);
const referenceVideoInput = ref(null);

const imageUpload = reactive(createUploadState());
const audioUpload = reactive(createUploadState());
const referenceVideoUpload = reactive(createUploadState());

const currentModel = computed(() => MODEL_OPTIONS.find((item) => item.value === form.model) || MODEL_OPTIONS[0]);
const supportsPromptExtend = computed(() => ['wan2.6-t2v', 'wan2.6-i2v'].includes(form.model));
const supportsAudio = computed(() => ['wan2.6-t2v', 'wan2.6-i2v'].includes(form.model));
const isAnyUploadBusy = computed(() => imageUpload.uploading || audioUpload.uploading || referenceVideoUpload.uploading);

let pollTimer = null;

function createUploadState() {
  return {
    name: '',
    preview: '',
    url: '',
    base64: '',
    status: '',
    error: '',
    uploading: false
  };
}

function resetUploadState(state, revokePreview = true) {
  if (revokePreview && state.preview?.startsWith('blob:')) {
    URL.revokeObjectURL(state.preview);
  }
  state.name = '';
  state.preview = '';
  state.url = '';
  state.base64 = '';
  state.status = '';
  state.error = '';
  state.uploading = false;
}

function clearUpload(state, inputRef) {
  resetUploadState(state);
  if (inputRef?.value) {
    inputRef.value.value = '';
  }
}

function applyModelDefaults(model) {
  form.model = model;
  form.shotType = 'multi';
  errorMessage.value = '';

  if (model === 'wan2.6-r2v-flash') {
    form.size = '1280*720';
    form.duration = 5;
    return;
  }

  if (model === 'wan2.6-i2v') {
    form.resolution = '720P';
  } else {
    form.size = '1280*720';
  }

  form.duration = 10;
  form.promptExtend = true;
  form.audio = true;
}

function applyPendingVideoTemplate() {
  const pending = consumePendingTemplate('video');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;

  if (typeof fields.model === 'string' && fields.model) {
    applyModelDefaults(fields.model);
  }
  if (typeof fields.prompt === 'string') {
    form.prompt = fields.prompt;
  }
  if (typeof fields.resolution === 'string' && fields.resolution) {
    form.resolution = fields.resolution;
  }
  if (typeof fields.size === 'string' && fields.size) {
    form.size = fields.size;
  }
  if (!Number.isNaN(Number(fields.duration)) && Number(fields.duration) > 0) {
    form.duration = Number(fields.duration);
  }
  if (['multi', 'single'].includes(fields.shotType)) {
    form.shotType = fields.shotType;
  }
  if (typeof fields.promptExtend === 'boolean') {
    form.promptExtend = fields.promptExtend;
  }
  if (typeof fields.audio === 'boolean') {
    form.audio = fields.audio;
  }
}

function triggerImageSelect() {
  imageInput.value?.click();
}

function triggerAudioSelect() {
  audioInput.value?.click();
}

function triggerReferenceVideoSelect() {
  referenceVideoInput.value?.click();
}

function buildPreview(file) {
  return URL.createObjectURL(file);
}

async function uploadAsset(file, state, uploader, successMessage, extraFields = {}) {
  resetUploadState(state);
  state.name = file.name;
  state.preview = buildPreview(file);
  state.status = '正在上传...';
  state.uploading = true;

  try {
    const formData = new FormData();
    formData.append('file', file);
    Object.entries(extraFields).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        formData.append(key, value);
      }
    });
    const { response, data } = await uploader(formData);
    if (!response.ok || !data?.success) {
      throw new Error(data?.message || '上传失败');
    }
    state.url = resolveAssetUrl(data.url || '');
    state.base64 = data.base64 || '';
    state.status = successMessage;
    state.error = '';
  } catch (error) {
    state.error = error.message || '上传失败';
    state.url = '';
    state.base64 = '';
  } finally {
    state.uploading = false;
  }
}

async function handleImageChange(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  await uploadAsset(file, imageUpload, api.uploadImage, '图片已上传，可用于图生视频。');
}

async function handleAudioChange(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  await uploadAsset(file, audioUpload, api.uploadAudio, '音频已上传，可作为视频音频输入。', { model: 'wan2.6-i2v' });
}

async function handleReferenceVideoChange(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  await uploadAsset(file, referenceVideoUpload, api.uploadVideo, '参考视频已上传，可用于视频模仿。', { model: 'wan2.6-r2v-flash' });
}

function buildPayload() {
  if (form.model === 'wan2.6-i2v') {
    return {
      model: form.model,
        input: {
          prompt: form.prompt.trim(),
          img_url: imageUpload.base64 || imageUpload.url,
          ...(form.audio && audioUpload.url ? { audio_url: audioUpload.url } : {})
        },
      parameters: {
        resolution: form.resolution,
        prompt_extend: form.promptExtend,
        duration: Number(form.duration),
        audio: form.audio,
        shot_type: form.shotType
      }
    };
  }

  if (form.model === 'wan2.6-r2v-flash') {
    return {
      model: form.model,
      input: {
        prompt: form.prompt.trim(),
        reference_urls: referenceVideoUpload.url ? [referenceVideoUpload.url] : []
      },
      parameters: {
        size: form.size,
        duration: Number(form.duration),
        shot_type: form.shotType
      }
    };
  }

  return {
    model: form.model,
    input: {
      prompt: form.prompt.trim()
    },
    parameters: {
      size: form.size,
      prompt_extend: form.promptExtend,
      duration: Number(form.duration),
      audio: form.audio,
      shot_type: form.shotType
    }
  };
}

function validate() {
  if (!form.prompt.trim()) return '请输入视频提示词。';
  if (form.model === 'wan2.6-i2v' && !(imageUpload.base64 || imageUpload.url)) return '请先上传图生视频的参考图片。';
  if (form.model === 'wan2.6-r2v-flash' && !referenceVideoUpload.url) return '请先上传参考视频。';
  return '';
}

function normalizeStatus(value) {
  return String(value || '').trim().toUpperCase();
}

function extractTaskId(data) {
  return data?.output?.task_id || '';
}

function extractStatus(data) {
  return normalizeStatus(data?.output?.task_status || '');
}

function extractVideoUrl(data) {
  if (data?.output?.video_url) return data.output.video_url;
  const results = data?.output?.results;
  if (Array.isArray(results)) {
    const match = results.find((item) => item?.video_url || item?.url);
    return match?.video_url || match?.url || '';
  }
  return '';
}

function extractError(data, fallback = '视频生成失败。') {
  return data?.message || data?.output?.message || data?.code || fallback;
}

function isTerminal(status) {
  return ['SUCCEEDED', 'FAILED', 'CANCELED'].includes(normalizeStatus(status));
}

function isFailure(status) {
  return ['FAILED', 'CANCELED'].includes(normalizeStatus(status));
}

function stopPolling() {
  if (pollTimer) {
    clearTimeout(pollTimer);
    pollTimer = null;
  }
}

function queuePoll(taskId) {
  stopPolling();
  pollTimer = setTimeout(() => pollTask(taskId, true), 15000);
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const { response, data } = await api.getVideoHistory();
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    history.value = Array.isArray(data) ? data : [];
  } finally {
    historyLoading.value = false;
  }
}

async function pollTask(taskId, silent = false) {
  if (!taskId || polling.value) return;
  polling.value = true;
  if (!silent) resultMode.value = 'polling';

  try {
    const { response, data } = await api.getVideoTask(taskId);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    rawResponse.value = JSON.stringify(data, null, 2);
    activeTaskStatus.value = extractStatus(data) || activeTaskStatus.value;

    const videoUrl = extractVideoUrl(data);
    if (videoUrl) {
      stopPolling();
      resultVideoUrl.value = videoUrl;
      resultMode.value = 'video';
      await loadHistory();
      await auth.fetchCurrentUser();
      return;
    }

    if (isTerminal(activeTaskStatus.value)) {
      stopPolling();
      resultMode.value = isFailure(activeTaskStatus.value) ? 'error' : 'raw';
      errorMessage.value = isFailure(activeTaskStatus.value) ? extractError(data) : '';
      await loadHistory();
      return;
    }

    queuePoll(taskId);
  } catch (error) {
    stopPolling();
    errorMessage.value = error.message || '任务轮询失败。';
    resultMode.value = 'error';
  } finally {
    polling.value = false;
  }
}

async function generateVideo() {
  const message = validate();
  if (message) {
    errorMessage.value = message;
    resultMode.value = 'error';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  rawResponse.value = '';
  resultVideoUrl.value = '';
  activeTaskId.value = '';
  activeTaskStatus.value = '';
  resultMode.value = 'submitting';
  stopPolling();

  try {
    const { response, data } = await api.generateVideo(buildPayload());
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    rawResponse.value = JSON.stringify(data, null, 2);
    activeTaskId.value = extractTaskId(data);
    activeTaskStatus.value = extractStatus(data) || 'PENDING';

    const videoUrl = extractVideoUrl(data);
    if (!response.ok && !activeTaskId.value && !videoUrl) {
      throw new Error(extractError(data));
    }

    if (videoUrl) {
      resultVideoUrl.value = videoUrl;
      resultMode.value = 'video';
      await loadHistory();
      return;
    }

    if (!activeTaskId.value) {
      throw new Error('接口没有返回任务 ID。');
    }

    resultMode.value = 'polling';
    await loadHistory();
    queuePoll(activeTaskId.value);
  } catch (error) {
    errorMessage.value = error.message || '视频生成失败。';
    resultMode.value = 'error';
  } finally {
    loading.value = false;
  }
}

function refreshCurrentTask() {
  if (activeTaskId.value) {
    stopPolling();
    pollTask(activeTaskId.value);
  }
}

function resumeTask(taskId) {
  activeTaskId.value = taskId;
  activeTaskStatus.value = 'PENDING';
  errorMessage.value = '';
  resultVideoUrl.value = '';
  resultMode.value = 'polling';
  stopPolling();
  pollTask(taskId);
}

function loadHistoryItem(item) {
  resultVideoUrl.value = item.videoUrl || '';
  activeTaskId.value = item.taskId || '';
  activeTaskStatus.value = normalizeStatus(item.taskStatus) || 'SUCCEEDED';
  rawResponse.value = item.rawResponse || '';
  resultMode.value = item.videoUrl ? 'video' : 'raw';
}

function formatStatus(status) {
  return normalizeStatus(status) || 'WAITING';
}

function statusTextClass(status) {
  if (isFailure(status)) return 'text-rose-300';
  if (normalizeStatus(status) === 'SUCCEEDED') return 'text-emerald-300';
  return 'text-sky-300';
}

function statusPillClass(status) {
  if (isFailure(status)) return 'text-rose-300 border-rose-500/30 bg-rose-900/20';
  if (normalizeStatus(status) === 'SUCCEEDED') return 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20';
  return 'text-sky-300 border-sky-500/30 bg-sky-900/20';
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

function parsePayload(payload) {
  try {
    return JSON.parse(payload);
  } catch (error) {
    return null;
  }
}

function parsePrompt(payload) {
  return parsePayload(payload)?.input?.prompt || '暂无提示词';
}

function parseSummary(payload) {
  const parsed = parsePayload(payload);
  if (!parsed) return '原始请求不可解析';

  const parts = [];
  if (parsed.input?.img_url) parts.push('图片');
  if (parsed.input?.audio_url) parts.push('音频');
  if (Array.isArray(parsed.input?.reference_urls)) parts.push(`参考视频 ${parsed.input.reference_urls.length} 个`);
  if (parsed.parameters?.resolution) parts.push(parsed.parameters.resolution);
  if (parsed.parameters?.size) parts.push(parsed.parameters.size);
  if (parsed.parameters?.duration) parts.push(`${parsed.parameters.duration}s`);
  return parts.join(' | ') || '纯文本';
}

function openVideo(url) {
  if (url) {
    window.open(url, '_blank');
  }
}

async function downloadVideo(url) {
  if (!url) return;

  try {
    const response = await fetch(url, { mode: 'cors' });
    const blob = await response.blob();
    const blobUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `ai_video_${Date.now()}.mp4`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(blobUrl);
  } catch (error) {
    openVideo(url);
  }
}

onMounted(async () => {
  applyPendingVideoTemplate();
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadHistory();
  }
});

onBeforeUnmount(() => {
  stopPolling();
  resetUploadState(imageUpload);
  resetUploadState(audioUpload);
  resetUploadState(referenceVideoUpload);
});
</script>

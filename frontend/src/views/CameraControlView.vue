<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.05fr_0.95fr] items-start">
          <div>
            <div class="studio-kicker">Camera Lab</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              3D 镜头控制器
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              上传一张原始图片，用可视化轨道去改视角、俯仰和景别。页面保留实验感，但把上传、控制和结果三个面板做成更清晰的联动关系。
            </p>
            <div class="mt-6 flex flex-wrap gap-3">
              <span class="studio-chip">不同视角重建</span>
              <span class="studio-chip">轨道可视化控制</span>
              <span class="studio-chip">镜头预设快捷切换</span>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">方位角</div>
              <div class="studio-metric-value">{{ form.azimuth }}°</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">俯仰角</div>
              <div class="studio-metric-value">{{ form.elevation }}°</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">景别</div>
              <div class="studio-metric-value">{{ distanceUIDesc }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div class="lg:col-span-4 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Reference Input</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">上传原始图片</h2>
                <p class="mt-2 text-sm text-slate-400">建议主体完整、边缘清晰，这样转换到新视角时结构更稳定。</p>
              </div>
              <div class="text-xs text-slate-500 uppercase tracking-[0.2em]">Single Image</div>
            </div>

            <div class="mt-6 studio-dropzone rounded-[1.75rem] p-5">
              <input ref="imageInput" type="file" accept="image/*" class="hidden" @change="handleFileChange">

              <div
                v-if="!upload.preview"
                class="studio-upload-placeholder cursor-pointer"
                @click="triggerFileSelect"
              >
                <div class="w-20 h-20 rounded-full border border-dashed border-slate-600 bg-slate-900/70 flex items-center justify-center text-sm text-slate-300">
                  上传
                </div>
                <div class="mt-5 text-lg font-bold text-slate-100">点击选择一张参考图</div>
                <div class="mt-2 text-sm text-slate-400">支持 JPG、PNG、WEBP。建议主体居中或结构完整。</div>
              </div>

              <div v-else class="space-y-4">
                <div class="rounded-[1.4rem] overflow-hidden border border-slate-700/60 bg-slate-950/60">
                  <img :src="upload.preview" class="w-full aspect-[4/5] object-cover cursor-pointer" alt="" @click="triggerFileSelect">
                </div>
                <div class="flex gap-3">
                  <button
                    type="button"
                    class="flex-1 px-4 py-3 rounded-xl bg-slate-800/80 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                    @click="triggerFileSelect"
                  >
                    更换图片
                  </button>
                  <button
                    type="button"
                    class="px-4 py-3 rounded-xl bg-rose-900/30 text-rose-300 border border-rose-800/60 hover:bg-rose-900/45 transition"
                    @click="clearImage"
                  >
                    清空
                  </button>
                </div>
              </div>
            </div>

            <div
              v-if="uploadStatus"
              class="mt-5 text-sm rounded-2xl border px-4 py-3"
              :class="uploadError ? 'text-rose-300 bg-rose-900/30 border-rose-800' : 'text-sky-300 bg-sky-900/30 border-sky-800/50'"
            >
              {{ uploadStatus }}
            </div>

            <div class="mt-5 grid gap-4 sm:grid-cols-2">
              <div class="studio-tip-card">
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">推荐做法</div>
                <div class="mt-3 text-sm text-slate-300 leading-7">
                  人像、产品、手办等主体明确的图片效果最好。
                </div>
              </div>
              <div class="studio-tip-card">
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">避免情况</div>
                <div class="mt-3 text-sm text-slate-300 leading-7">
                  过暗、遮挡严重或主体裁切太多时，视角重建容易漂。
                </div>
              </div>
            </div>
          </section>
        </div>

        <div class="lg:col-span-4 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Lens Controls</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">镜头参数面板</h2>
              </div>
              <select
                v-model="form.model"
                class="w-full sm:w-64 bg-slate-900/70 border border-slate-700 text-slate-200 rounded-2xl px-4 py-3 outline-none neon-border"
              >
                <option value="qwen-image-2.0-pro">Qwen Image 2.0 Pro</option>
                <option value="qwen-image-2.0">Qwen Image 2.0</option>
                <option value="qwen-image-max">Qwen Image Max</option>
              </select>
            </div>

            <div class="mt-6 rounded-[1.75rem] border border-slate-700/60 bg-slate-950/45 p-5">
              <div class="grid grid-cols-12 items-center gap-4">
                <div class="col-span-2 flex flex-col items-center h-52 justify-between py-4">
                  <div class="camera-axis-label">俯仰</div>
                  <input
                    v-model.number="form.elevation"
                    type="range"
                    min="-90"
                    max="90"
                    class="w-36 -rotate-90 appearance-none bg-slate-700 h-1.5 rounded-full cursor-pointer"
                  >
                </div>

                <div class="col-span-8 flex items-center justify-center">
                  <div class="w-full max-w-[320px] aspect-square rounded-full border border-slate-700/70 bg-slate-950/65 p-5">
                    <svg viewBox="0 0 200 200" class="w-full h-full drop-shadow-md">
                      <circle cx="100" cy="100" r="90" fill="#0b0f19" stroke="#334155" stroke-width="2" />
                      <ellipse cx="100" cy="100" rx="45" ry="90" fill="none" stroke="#334155" stroke-width="1.5" />
                      <ellipse cx="100" cy="100" rx="15" ry="90" fill="none" stroke="#334155" stroke-width="1.5" />
                      <line x1="100" y1="10" x2="100" y2="190" stroke="#475569" stroke-width="1.5" stroke-dasharray="4 4" />
                      <ellipse cx="100" cy="100" rx="90" ry="30" fill="none" stroke="#334155" stroke-width="1.5" />
                      <line x1="10" y1="100" x2="190" y2="100" stroke="#475569" stroke-width="1.5" stroke-dasharray="4 4" />
                      <rect x="85" y="80" width="30" height="40" rx="4" fill="#1e293b" stroke="#64748b" stroke-width="2" opacity="0.8" />
                      <text x="100" y="105" font-family="sans-serif" font-size="14" font-weight="bold" fill="#94a3b8" text-anchor="middle">F</text>
                      <g :transform="cameraTransform" :style="{ opacity: cameraOpacity }">
                        <circle cx="0" cy="0" r="16" fill="#fef08a" stroke="#f59e0b" stroke-width="2" opacity="0.4" />
                        <circle cx="0" cy="0" r="12" fill="#fbbf24" />
                        <circle cx="0" cy="0" r="2.5" fill="white" />
                      </g>
                    </svg>
                  </div>
                </div>

                <div class="col-span-2 flex flex-col items-center h-52 justify-between py-4">
                  <div class="camera-axis-label">景别</div>
                  <input
                    v-model.number="form.distance"
                    type="range"
                    min="0.5"
                    max="2.0"
                    step="0.1"
                    class="w-36 rotate-90 appearance-none bg-slate-700 h-1.5 rounded-full cursor-pointer"
                  >
                </div>
              </div>

              <div class="mt-5">
                <div class="flex items-center justify-between gap-3 mb-3">
                  <div class="text-sm font-bold text-slate-200">水平旋转</div>
                  <div class="text-sm text-slate-400">{{ form.azimuth }}°</div>
                </div>
                <input
                  v-model.number="form.azimuth"
                  type="range"
                  min="0"
                  max="360"
                  class="w-full appearance-none bg-slate-700 h-1.5 rounded-full cursor-pointer"
                >
              </div>
            </div>

            <div class="mt-6 grid grid-cols-3 gap-3">
              <div class="camera-stat-card">
                <div class="camera-stat-label">视角</div>
                <div class="camera-stat-value">{{ azimuthUIDesc }}</div>
              </div>
              <div class="camera-stat-card">
                <div class="camera-stat-label">俯仰</div>
                <div class="camera-stat-value">{{ elevationUIDesc }}</div>
              </div>
              <div class="camera-stat-card">
                <div class="camera-stat-label">景别</div>
                <div class="camera-stat-value">{{ distanceUIDesc }}</div>
              </div>
            </div>

            <div class="mt-6">
              <div class="text-sm font-bold text-slate-300 mb-3">快捷预设</div>
              <div class="grid grid-cols-2 gap-3">
                <button
                  v-for="preset in presets"
                  :key="preset.title"
                  type="button"
                  class="camera-preset text-left"
                  @click="applyPreset(preset)"
                >
                  <div class="text-sm font-bold text-slate-100">{{ preset.title }}</div>
                  <div class="mt-1 text-xs text-slate-400">{{ preset.note }}</div>
                </button>
              </div>
            </div>

            <button
              class="mt-6 w-full py-4 rounded-2xl font-bold transition-all bg-gradient-to-r from-sky-600 to-indigo-600 text-white hover:from-sky-500 hover:to-indigo-500 btn-glow"
              :class="{ 'opacity-60 cursor-not-allowed': !canGenerate }"
              :disabled="!canGenerate"
              @click="generateImage"
            >
              {{ loading ? '正在生成...' : '应用镜头参数' }}
            </button>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Prompt Preview</div>
            <h2 class="mt-2 text-xl font-bold text-slate-100">提交给模型的镜头说明</h2>
            <div class="camera-prompt-card mt-5 whitespace-pre-line">
              {{ promptText }}
            </div>
          </section>
        </div>

        <div class="lg:col-span-4 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Render Output</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">结果预览</h2>
                <p class="mt-2 text-sm text-slate-400">右侧输出会直观反映当前镜头参数，如果变化不够明显，可以直接切换预设再生成。</p>
              </div>
            </div>

            <div class="studio-stage rounded-[1.75rem] p-4 min-h-[700px]">
              <div v-if="resultState === 'loading'" class="h-full flex flex-col items-center justify-center text-center px-8">
                <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                  <div class="w-7 h-7 rounded-full bg-sky-400/70" />
                </div>
                <div class="mt-5 text-lg font-bold text-slate-100">正在渲染新视角</div>
                <div class="mt-2 text-sm text-slate-400">会尽量保持主体身份一致，同时做出明显的透视变化。</div>
              </div>

              <div v-else-if="resultState === 'image'" class="space-y-4">
                <div class="studio-stage-checker rounded-[1.5rem] overflow-hidden border border-slate-700/60">
                  <img :src="resultImageUrl" class="w-full min-h-[620px] max-h-[760px] object-cover cursor-pointer" alt="" @click="openResult">
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                    @click="openResult"
                  >
                    查看大图
                  </button>
                  <button
                    type="button"
                    class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                    @click="downloadResult"
                  >
                    下载图片
                  </button>
                </div>
              </div>

              <div v-else-if="resultState === 'raw'" class="rounded-[1.5rem] border border-yellow-800 bg-yellow-900/20 p-5 text-yellow-200">
                <div class="font-bold mb-3">API 已返回结果，但未解析出图片链接</div>
                <pre class="text-xs whitespace-pre-wrap overflow-auto">{{ rawResponse }}</pre>
              </div>

              <div v-else-if="resultState === 'error'" class="rounded-[1.5rem] border border-rose-800 bg-rose-900/25 p-5 text-rose-200">
                {{ errorMessage }}
              </div>

              <div v-else class="h-full flex items-center justify-center">
                <div class="studio-empty max-w-sm text-center">
                  <div class="text-xs uppercase tracking-[0.24em] text-slate-500">Output Waiting</div>
                  <div class="mt-4 text-2xl font-bold text-slate-100">先上传图片，再调整镜头</div>
                  <div class="mt-3 text-sm leading-7 text-slate-400">
                    当前推荐视角：{{ azimuthUIDesc }} / {{ elevationUIDesc }} / {{ distanceUIDesc }}
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import { consumePendingTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

function normalizeAzimuth(value) {
  const angle = Number.parseInt(value, 10);
  return ((angle % 360) + 360) % 360;
}

function getAzimuthDescForPrompt(value) {
  const angle = normalizeAzimuth(value);
  if (angle >= 337.5 || angle < 22.5) return `front view (${angle}°)`;
  if (angle < 67.5) return `front-right 3/4 view (${angle}°)`;
  if (angle < 112.5) return `right side view (${angle}°)`;
  if (angle < 157.5) return `back-right 3/4 view (${angle}°)`;
  if (angle < 202.5) return `back view (${angle}°)`;
  if (angle < 247.5) return `back-left 3/4 view (${angle}°)`;
  if (angle < 292.5) return `left side view (${angle}°)`;
  return `front-left 3/4 view (${angle}°)`;
}

function getAzimuthUIDesc(value) {
  const angle = normalizeAzimuth(value);
  if (angle >= 337.5 || angle < 22.5) return '正面';
  if (angle < 67.5) return '右前 3/4';
  if (angle < 112.5) return '右侧';
  if (angle < 157.5) return '右后 3/4';
  if (angle < 202.5) return '背面';
  if (angle < 247.5) return '左后 3/4';
  if (angle < 292.5) return '左侧';
  return '左前 3/4';
}

function getElevationDescForPrompt(value) {
  const angle = Number.parseInt(value, 10);
  if (angle >= 55) return 'strong top-down view';
  if (angle >= 20) return 'high angle view';
  if (angle <= -55) return 'strong low angle view';
  if (angle <= -20) return 'low angle view';
  return 'eye-level view';
}

function getElevationUIDesc(value) {
  const angle = Number.parseInt(value, 10);
  if (angle >= 55) return '俯视';
  if (angle >= 20) return '高机位';
  if (angle <= -55) return '仰视';
  if (angle <= -20) return '低机位';
  return '平视';
}

function getDistanceDescForPrompt(value) {
  const amount = Number.parseFloat(value);
  if (amount < 0.8) return 'close-up shot';
  if (amount < 1.35) return 'medium shot';
  if (amount < 1.75) return 'full body shot';
  return 'wide shot';
}

function getDistanceUIDesc(value) {
  const amount = Number.parseFloat(value);
  if (amount < 0.8) return '特写';
  if (amount < 1.35) return '中景';
  if (amount < 1.75) return '全身';
  return '远景';
}

const presets = [
  { title: '正面中景', note: '适合先看主体结构', azimuth: 0, elevation: 0, distance: 1.1 },
  { title: '右前高机位', note: '适合产品和角色', azimuth: 45, elevation: 28, distance: 0.9 },
  { title: '左侧平视', note: '强调侧面轮廓', azimuth: 270, elevation: 0, distance: 1.2 },
  { title: '背面远景', note: '拉开空间关系', azimuth: 180, elevation: 12, distance: 1.9 }
];

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  model: 'qwen-image-2.0',
  azimuth: 45,
  elevation: 20,
  distance: 0.8
});
const upload = reactive({
  preview: '',
  base64: '',
  url: ''
});
const imageInput = ref(null);
const uploading = ref(false);
const uploadStatus = ref('');
const uploadError = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const resultState = ref('empty');
const resultImageUrl = ref('');
const rawResponse = ref('');

const azimuthUIDesc = computed(() => getAzimuthUIDesc(form.azimuth));
const elevationUIDesc = computed(() => getElevationUIDesc(form.elevation));
const distanceUIDesc = computed(() => getDistanceUIDesc(form.distance));
const canGenerate = computed(() => !loading.value && !uploading.value && Boolean(upload.base64));

const promptText = computed(() => `Use the input image as the ONLY reference.

Reconstruct the subject from a CLEARLY DIFFERENT camera angle.

The original viewpoint is NOT allowed.

Camera transformation:
- rotate the camera to ${getAzimuthDescForPrompt(form.azimuth)}
- camera elevation: ${getElevationDescForPrompt(form.elevation)}
- camera distance: ${getDistanceDescForPrompt(form.distance)}

The new viewpoint must be visually and structurally different from the input image.
Keep identity identical.
Ensure correct 3D perspective and geometry.`);

const cameraTransform = computed(() => {
  const angle = normalizeAzimuth(form.azimuth);
  const tilt = Number.parseInt(form.elevation, 10);
  const shot = Number.parseFloat(form.distance);
  const radAzi = angle * (Math.PI / 180);
  const radEle = tilt * (Math.PI / 180);
  const centerX = 100;
  const centerY = 100;
  const orbitRadius = 90;
  const perspectiveYOffset = 18;
  const x3d = Math.sin(radAzi) * Math.cos(radEle);
  const y3d = -Math.sin(radEle);
  const z3d = Math.cos(radAzi) * Math.cos(radEle);
  const x = centerX + x3d * orbitRadius;
  const y = centerY + y3d * orbitRadius * 0.78 + z3d * perspectiveYOffset;
  const baseScale = 1.22 - ((shot - 0.5) / 1.5) * 0.5;
  const depthScale = 0.78 + ((z3d + 1) / 2) * 0.34;
  return `translate(${x.toFixed(2)}, ${y.toFixed(2)}) scale(${(baseScale * depthScale).toFixed(3)})`;
});

const cameraOpacity = computed(() => {
  const angle = normalizeAzimuth(form.azimuth);
  const tilt = Number.parseInt(form.elevation, 10);
  const radAzi = angle * (Math.PI / 180);
  const radEle = tilt * (Math.PI / 180);
  const z3d = Math.cos(radAzi) * Math.cos(radEle);
  return (0.35 + ((z3d + 1) / 2) * 0.65).toFixed(2);
});

function applyPreset(preset) {
  form.azimuth = preset.azimuth;
  form.elevation = preset.elevation;
  form.distance = preset.distance;
}

function applyPendingCameraTemplate() {
  const pending = consumePendingTemplate('camera');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.model === 'string' && fields.model) {
    form.model = fields.model;
  }
  if (!Number.isNaN(Number(fields.azimuth))) {
    form.azimuth = Number(fields.azimuth);
  }
  if (!Number.isNaN(Number(fields.elevation))) {
    form.elevation = Number(fields.elevation);
  }
  if (!Number.isNaN(Number(fields.distance))) {
    form.distance = Number(fields.distance);
  }
}

function triggerFileSelect() {
  imageInput.value?.click();
}

async function handleFileChange(event) {
  const file = event.target.files?.[0];
  event.target.value = '';
  if (!file) {
    return;
  }

  uploading.value = true;
  uploadStatus.value = '正在上传并解析参考图...';
  uploadError.value = false;
  errorMessage.value = '';

  try {
    upload.preview = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (loadEvent) => resolve(loadEvent.target.result);
      reader.onerror = () => reject(new Error('读取文件失败'));
      reader.readAsDataURL(file);
    });

    const formData = new FormData();
    formData.append('file', file);
    const { response, data } = await api.uploadImage(formData);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!data.success) {
      throw new Error(data.message || '图片处理失败');
    }

    upload.base64 = data.base64 || '';
    upload.url = resolveAssetUrl(data.url || '');
    uploadStatus.value = '参考图已就绪，可以开始生成。';
  } catch (error) {
    uploadStatus.value = error.message;
    uploadError.value = true;
    errorMessage.value = error.message;
    clearImage();
  } finally {
    uploading.value = false;
  }
}

function clearImage() {
  upload.preview = '';
  upload.base64 = '';
  upload.url = '';
  uploadStatus.value = '';
  uploadError.value = false;
}

function parseImageUrls(data) {
  if (Array.isArray(data.output?.results)) {
    return data.output.results.map((item) => item.url).filter(Boolean);
  }
  const contents = data.output?.choices?.[0]?.message?.content;
  if (Array.isArray(contents)) {
    return contents.map((item) => item.image).filter(Boolean);
  }
  return [];
}

async function generateImage() {
  if (!upload.base64) {
    errorMessage.value = '请先上传一张原始图片。';
    resultState.value = 'error';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  resultState.value = 'loading';
  resultImageUrl.value = '';
  rawResponse.value = '';

  const requestBody = {
    model: form.model,
    input: {
      messages: [
        {
          role: 'user',
          content: [
            { image: upload.base64 },
            { text: promptText.value }
          ]
        }
      ]
    },
    parameters: { n: 1 }
  };

  try {
    const { response, data } = await api.generateCamera(requestBody, {
      localUrl: upload.url,
      azimuth: form.azimuth,
      elevation: form.elevation,
      distance: form.distance
    });
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    const imageUrls = parseImageUrls(data);
    if (imageUrls.length > 0) {
      resultImageUrl.value = imageUrls[0];
      resultState.value = 'image';
      await auth.fetchCurrentUser();
      return;
    }
    if (data.code && data.message) {
      throw new Error(`${data.code} ${data.message}`);
    }
    rawResponse.value = JSON.stringify(data, null, 2);
    resultState.value = 'raw';
  } catch (error) {
    errorMessage.value = error.message;
    resultState.value = 'error';
  } finally {
    loading.value = false;
  }
}

function openResult() {
  if (resultImageUrl.value) {
    window.open(resultImageUrl.value, '_blank');
  }
}

async function downloadResult() {
  if (!resultImageUrl.value) {
    return;
  }

  try {
    const response = await fetch(resultImageUrl.value, { mode: 'cors' });
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `3d_render_${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  } catch (error) {
    const link = document.createElement('a');
    link.href = resultImageUrl.value;
    link.download = `3d_render_${Date.now()}.png`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

onMounted(async () => {
  applyPendingCameraTemplate();
  await auth.ensureAuthenticated(router, route.fullPath);
});
</script>

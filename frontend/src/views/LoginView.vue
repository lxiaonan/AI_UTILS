<template>
  <div class="login-page min-h-screen px-4 py-6 md:py-8 text-slate-100 overflow-hidden flex items-center justify-center">
    <div class="login-noise" />
    <div class="login-glow login-glow-a" />
    <div class="login-glow login-glow-b" />

    <div class="relative w-full mx-auto max-w-7xl grid gap-6 lg:grid-cols-[1.15fr_0.85fr] items-stretch">
      <section class="login-hero rounded-[2.4rem] p-6 md:p-8 lg:p-10 overflow-hidden flex flex-col justify-between">
        <div class="login-hero-grid" />
        <div class="login-scanline" />

        <div class="relative z-10">
          <div class="inline-flex items-center gap-3 rounded-full border border-sky-400/20 bg-slate-950/40 px-4 py-2 text-[11px] uppercase tracking-[0.34em] text-sky-200/80">
            <span class="login-status-dot" />
            Neural Access
          </div>

          <h1 class="mt-8 max-w-4xl text-4xl md:text-5xl xl:text-6xl font-extrabold leading-[1.05] text-slate-50">
            进入你的
            <span class="bg-gradient-to-r from-sky-200 via-cyan-300 to-indigo-300 bg-clip-text text-transparent">
              AI 控制界面
            </span>
          </h1>

          <p class="mt-6 max-w-2xl text-base md:text-lg leading-8 text-slate-300/90">
            多模态能力统一接入，进入后直接开始工作。
          </p>
        </div>

        <div class="relative z-10 mt-10 grid gap-4 lg:grid-cols-[0.9fr_1.1fr] items-end">
          <div class="login-orbit-card">
            <div class="login-orbit-ring login-orbit-ring-a" />
            <div class="login-orbit-ring login-orbit-ring-b" />
            <div class="login-orbit-core">
              <div class="text-[10px] uppercase tracking-[0.34em] text-slate-400">Core Link</div>
              <div class="mt-3 text-3xl font-black text-slate-50">AI</div>
              <div class="mt-2 text-sm text-slate-400">Secure node active</div>
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-3">
            <div class="login-stat-card">
              <div class="login-stat-label">SQL</div>
              <div class="login-stat-value">诊断矩阵</div>
              <div class="login-stat-desc">结构化规则与模型建议联动</div>
            </div>
            <div class="login-stat-card">
              <div class="login-stat-label">IMAGE</div>
              <div class="login-stat-value">生成引擎</div>
              <div class="login-stat-desc">多模态输入统一投喂</div>
            </div>
            <div class="login-stat-card">
              <div class="login-stat-label">VOICE</div>
              <div class="login-stat-value">合成节点</div>
              <div class="login-stat-desc">多语言多音色快速切换</div>
            </div>
          </div>
        </div>
      </section>

      <section class="login-panel rounded-[2.4rem] p-6 md:p-8 lg:p-10 relative overflow-hidden">
        <div class="login-panel-beam" />

        <div class="relative z-10">
          <div class="flex items-center justify-between gap-4 flex-wrap">
            <div class="flex items-center gap-4">
              <img
                :src="logoUrl"
                class="w-16 h-16 rounded-[1.6rem] shadow-[0_0_40px_rgba(99,102,241,0.32)] ring-1 ring-sky-400/30 bg-slate-950/50"
                alt="AI Platform"
              >
              <div>
                <div class="text-xs uppercase tracking-[0.28em] text-slate-500">AI Platform</div>
                <div class="mt-1 text-3xl font-extrabold text-slate-50">身份校验</div>
              </div>
            </div>
            <div class="login-mini-badge">
              <span class="login-status-dot" />
              Secure
            </div>
          </div>

          <div class="mt-8 space-y-5" @keydown.enter="doLogin">
            <div>
              <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-[0.18em]">用户名</label>
              <div class="login-input-shell">
                <input
                  v-model.trim="username"
                  class="login-input"
                  autocomplete="username"
                  placeholder="请输入用户名"
                >
              </div>
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-[0.18em]">密码</label>
              <div class="login-input-shell">
                <input
                  v-model="password"
                  type="password"
                  class="login-input"
                  autocomplete="current-password"
                  placeholder="请输入密码"
                >
              </div>
            </div>

            <label class="login-remember">
              <input v-model="rememberPassword" type="checkbox" class="login-remember-checkbox">
              <span>Remember password on this device</span>
            </label>

            <div class="login-telemetry">
              <div class="login-telemetry-row">
                <span>Auth Channel</span>
                <span>{{ loading ? 'Handshaking' : 'Standby' }}</span>
              </div>
              <div class="login-progress">
                <div class="login-progress-bar" :class="{ 'is-busy': loading }" />
              </div>
            </div>

            <div v-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ errorMessage }}
            </div>

            <button
              class="login-submit"
              :class="{ 'opacity-60 cursor-not-allowed': loading }"
              :disabled="loading"
              @click="doLogin"
            >
              <span class="relative z-10">{{ loading ? '正在验证身份...' : '进入控制台' }}</span>
            </button>
          </div>

          <div class="mt-8 flex items-center justify-between gap-4 text-sm text-slate-400 flex-wrap">
            <RouterLink to="/" class="hover:text-sky-300 transition">返回首页</RouterLink>
            <RouterLink to="/register" class="hover:text-sky-300 transition">没有账号？去注册</RouterLink>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@/lib/api';
import { useAuthStore } from '@/stores/auth';

const REMEMBER_LOGIN_KEY = 'aiset:remembered-login';

const router = useRouter();
const auth = useAuthStore();

const username = ref('');
const password = ref('');
const rememberPassword = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const logoUrl = '/logo/ai-logo.png';

function loadRememberedLogin() {
  try {
    const raw = window.localStorage.getItem(REMEMBER_LOGIN_KEY);
    if (!raw) {
      return;
    }
    const saved = JSON.parse(raw);
    username.value = saved.username || '';
    password.value = saved.password || '';
    rememberPassword.value = Boolean(saved.remember);
  } catch (error) {
    window.localStorage.removeItem(REMEMBER_LOGIN_KEY);
  }
}

function persistRememberedLogin() {
  if (!rememberPassword.value) {
    window.localStorage.removeItem(REMEMBER_LOGIN_KEY);
    return;
  }

  window.localStorage.setItem(REMEMBER_LOGIN_KEY, JSON.stringify({
    username: username.value.trim(),
    password: password.value,
    remember: true
  }));
}

async function doLogin() {
  if (loading.value) {
    return;
  }

  errorMessage.value = '';
  loading.value = true;

  try {
    const data = await api.login({
      username: username.value.trim(),
      password: password.value
    });

    if (!data.success) {
      errorMessage.value = data.message || '登录失败';
      return;
    }

    persistRememberedLogin();
    await auth.fetchCurrentUser();
    const redirect = auth.consumeRedirect();
    await router.replace(redirect || '/');
  } catch (error) {
    errorMessage.value = `请求失败：${error.message}`;
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  loadRememberedLogin();
  const data = await auth.fetchCurrentUser();
  if (data && data.success) {
    await router.replace('/');
  }
});
</script>

<style scoped>
.login-page {
  position: relative;
  background:
    radial-gradient(circle at 15% 20%, rgba(56, 189, 248, 0.14), transparent 28%),
    radial-gradient(circle at 85% 18%, rgba(99, 102, 241, 0.14), transparent 30%),
    radial-gradient(circle at 50% 100%, rgba(34, 211, 238, 0.1), transparent 38%);
}

.login-noise {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.18) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.16) 1px, transparent 1px);
  background-size: 72px 72px;
  mask-image: radial-gradient(circle at center, black 45%, transparent 90%);
}

.login-glow {
  position: absolute;
  width: 28rem;
  height: 28rem;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.32;
  pointer-events: none;
  animation: loginFloat 12s ease-in-out infinite alternate;
}

.login-glow-a {
  top: -10rem;
  right: -8rem;
  background: rgba(56, 189, 248, 0.28);
}

.login-glow-b {
  bottom: -12rem;
  left: -8rem;
  background: rgba(99, 102, 241, 0.24);
  animation-duration: 16s;
}

.login-hero,
.login-panel {
  position: relative;
  border: 1px solid rgba(125, 211, 252, 0.14);
  backdrop-filter: blur(20px);
  box-shadow:
    0 28px 80px rgba(2, 6, 23, 0.48),
    inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.login-hero {
  background:
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.16), transparent 30%),
    linear-gradient(145deg, rgba(7, 14, 28, 0.96), rgba(13, 24, 42, 0.82));
}

.login-panel {
  background:
    radial-gradient(circle at top, rgba(56, 189, 248, 0.12), transparent 28%),
    linear-gradient(145deg, rgba(8, 15, 30, 0.96), rgba(15, 25, 42, 0.82));
}

.login-hero-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(125, 211, 252, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(125, 211, 252, 0.08) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.9), transparent 85%);
}

.login-scanline {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.login-scanline::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: -20%;
  height: 26%;
  background: linear-gradient(180deg, transparent, rgba(56, 189, 248, 0.12), transparent);
  animation: scanSweep 7s linear infinite;
}

.login-status-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 999px;
  background: #38bdf8;
  box-shadow: 0 0 16px rgba(56, 189, 248, 0.8);
  animation: pulseDot 1.8s ease-in-out infinite;
}

.login-orbit-card {
  position: relative;
  min-height: 230px;
  border-radius: 1.8rem;
  border: 1px solid rgba(125, 211, 252, 0.16);
  background: radial-gradient(circle at center, rgba(56, 189, 248, 0.06), rgba(2, 6, 23, 0.72) 70%);
  overflow: hidden;
}

.login-orbit-ring {
  position: absolute;
  inset: 50% auto auto 50%;
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.18);
  transform: translate(-50%, -50%);
}

.login-orbit-ring-a {
  width: 160px;
  height: 160px;
  animation: spinSlow 14s linear infinite;
}

.login-orbit-ring-b {
  width: 220px;
  height: 220px;
  border-style: dashed;
  animation: spinSlowReverse 18s linear infinite;
}

.login-orbit-core {
  position: absolute;
  inset: 50% auto auto 50%;
  width: 118px;
  height: 118px;
  transform: translate(-50%, -50%);
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.22);
  background: linear-gradient(145deg, rgba(9, 16, 32, 0.96), rgba(15, 25, 42, 0.84));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  box-shadow:
    0 0 40px rgba(56, 189, 248, 0.1),
    inset 0 0 30px rgba(56, 189, 248, 0.08);
}

.login-stat-card {
  border-radius: 1.4rem;
  border: 1px solid rgba(125, 211, 252, 0.12);
  background:
    linear-gradient(145deg, rgba(9, 16, 32, 0.88), rgba(12, 22, 39, 0.68)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.08), transparent 45%);
  padding: 1rem;
}

.login-stat-label {
  font-size: 0.68rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #94a3b8;
}

.login-stat-value {
  margin-top: 0.7rem;
  font-size: 1.08rem;
  font-weight: 700;
  color: #f8fbff;
}

.login-stat-desc {
  margin-top: 0.5rem;
  font-size: 0.84rem;
  line-height: 1.7;
  color: #94a3b8;
}

.login-panel-beam {
  position: absolute;
  inset: -30% auto auto -10%;
  width: 140%;
  height: 180px;
  background: linear-gradient(90deg, transparent, rgba(56, 189, 248, 0.14), transparent);
  transform: rotate(-8deg);
  filter: blur(24px);
  animation: beamDrift 10s ease-in-out infinite alternate;
}

.login-mini-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.65rem 0.9rem;
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.18);
  background: rgba(2, 6, 23, 0.45);
  color: #dbeafe;
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.login-input-shell {
  position: relative;
  border-radius: 1.2rem;
  padding: 1px;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.16), rgba(99, 102, 241, 0.14), rgba(56, 189, 248, 0.08));
}

.login-input {
  width: 100%;
  border: none;
  outline: none;
  border-radius: 1.15rem;
  padding: 1rem 1rem;
  background: rgba(2, 6, 23, 0.82);
  color: #e2e8f0;
}

.login-input::placeholder {
  color: #64748b;
}

.login-remember {
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
  color: #cbd5e1;
  font-size: 0.92rem;
  user-select: none;
}

.login-remember-checkbox {
  width: 1rem;
  height: 1rem;
  accent-color: #38bdf8;
}

.login-telemetry {
  padding: 1rem 1.05rem;
  border-radius: 1.2rem;
  border: 1px solid rgba(125, 211, 252, 0.12);
  background: rgba(2, 6, 23, 0.45);
}

.login-telemetry-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  font-size: 0.76rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #94a3b8;
}

.login-progress {
  margin-top: 0.85rem;
  height: 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.9);
  overflow: hidden;
}

.login-progress-bar {
  height: 100%;
  width: 34%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.6), rgba(99, 102, 241, 0.9));
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.35);
}

.login-progress-bar.is-busy {
  width: 100%;
  animation: progressRun 1.3s linear infinite;
}

.login-submit {
  position: relative;
  width: 100%;
  overflow: hidden;
  border: none;
  border-radius: 1.35rem;
  padding: 1rem 1.25rem;
  font-size: 1.05rem;
  font-weight: 800;
  color: white;
  background: linear-gradient(90deg, #0284c7, #4f46e5, #0ea5e9);
  background-size: 180% 180%;
  animation: gradientShift 6s ease infinite;
  box-shadow:
    0 18px 44px rgba(37, 99, 235, 0.26),
    inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.login-submit::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: translateX(-120%);
  animation: submitSweep 3.4s ease-in-out infinite;
}

@keyframes loginFloat {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  100% {
    transform: translate3d(24px, -18px, 0) scale(1.05);
  }
}

@keyframes scanSweep {
  0% {
    top: -28%;
  }
  100% {
    top: 108%;
  }
}

@keyframes pulseDot {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.18);
  }
}

@keyframes spinSlow {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

@keyframes spinSlowReverse {
  from {
    transform: translate(-50%, -50%) rotate(360deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(0deg);
  }
}

@keyframes beamDrift {
  0% {
    transform: translate3d(-4%, 0, 0) rotate(-8deg);
  }
  100% {
    transform: translate3d(6%, 12px, 0) rotate(-5deg);
  }
}

@keyframes progressRun {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes submitSweep {
  0%,
  45% {
    transform: translateX(-120%);
  }
  70%,
  100% {
    transform: translateX(140%);
  }
}

@media (max-width: 768px) {
  .login-orbit-card {
    min-height: 200px;
  }

  .login-mini-badge {
    width: 100%;
    justify-content: center;
  }
}
</style>

<template>
  <div class="min-h-screen px-4 py-8 md:py-10 text-slate-100">
    <div class="mx-auto max-w-7xl grid gap-6 lg:grid-cols-[0.92fr_1.08fr] items-stretch">
      <section class="glass-panel rounded-[2.2rem] p-6 md:p-8 lg:p-10 relative overflow-hidden order-2 lg:order-1">
        <div class="absolute inset-x-0 top-0 h-40 bg-[radial-gradient(circle_at_top,rgba(244,114,182,0.16),transparent_70%)] pointer-events-none" />

        <div class="relative">
          <div class="flex items-center gap-4">
            <img
              :src="logoUrl"
              class="w-16 h-16 rounded-[1.6rem] shadow-[0_0_40px_rgba(236,72,153,0.35)] ring-1 ring-pink-400/30"
              alt="AI Platform"
            >
            <div>
              <div class="text-xs uppercase tracking-[0.24em] text-slate-500">Create Access</div>
              <div class="mt-1 text-3xl font-extrabold gradient-text">创建账号</div>
            </div>
          </div>

          <div class="mt-8 grid gap-4 sm:grid-cols-2">
            <div class="studio-tip-card">
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">自动登录</div>
              <div class="mt-3 text-sm text-slate-300 leading-7">注册成功后直接进入工作台，不需要再手动跳一次登录。</div>
            </div>
            <div class="studio-tip-card">
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">保留上下文</div>
              <div class="mt-3 text-sm text-slate-300 leading-7">如果你是从某个业务页跳来的，注册后会继续回到原目标页。</div>
            </div>
          </div>

          <div class="mt-8 space-y-5" @keydown.enter="doRegister">
            <div>
              <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-[0.18em]">用户名</label>
              <input
                v-model.trim="username"
                class="w-full bg-slate-950/65 border border-slate-700 rounded-2xl px-4 py-4 outline-none neon-border text-slate-100"
                placeholder="4-50 位，建议使用英文或数字"
              >
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-[0.18em]">密码</label>
              <input
                v-model="password"
                type="password"
                class="w-full bg-slate-950/65 border border-slate-700 rounded-2xl px-4 py-4 outline-none neon-border text-slate-100"
                placeholder="至少 6 位"
              >
            </div>

            <div v-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ errorMessage }}
            </div>

            <button
              class="w-full bg-gradient-to-r from-pink-600 to-indigo-600 hover:from-pink-500 hover:to-indigo-500 text-white font-bold py-4 rounded-2xl transition-all btn-glow text-lg"
              :class="{ 'opacity-60 cursor-not-allowed': loading }"
              :disabled="loading"
              @click="doRegister"
            >
              {{ loading ? '正在注册...' : '创建并进入工作台' }}
            </button>
          </div>

          <div class="mt-8 flex items-center justify-between gap-4 text-sm text-slate-400 flex-wrap">
            <RouterLink to="/" class="hover:text-pink-300 transition">返回首页</RouterLink>
            <RouterLink to="/login" class="hover:text-pink-300 transition">已有账号？去登录</RouterLink>
          </div>
        </div>
      </section>

      <section class="studio-hero rounded-[2.2rem] p-6 md:p-8 lg:p-10 overflow-hidden order-1 lg:order-2 flex flex-col justify-between">
        <div>
          <div class="studio-kicker">Registration</div>
          <h1 class="mt-6 text-4xl md:text-5xl xl:text-6xl font-extrabold leading-tight text-slate-50">
            让你的
            <span class="text-pink-300">SQL、图像与语音工作台</span>
            从这里接上
          </h1>
          <p class="mt-5 max-w-2xl text-slate-300 leading-8 text-base md:text-lg">
            前端已经开始收口成独立工程，注册页也一起统一到了新的视觉体系里，后续做独立部署会更顺手。
          </p>
        </div>

        <div class="mt-10 grid gap-4 sm:grid-cols-3">
          <div class="studio-metric">
            <div class="studio-metric-label">Flow</div>
            <div class="studio-metric-value">自动回跳</div>
            <div class="mt-2 text-sm text-slate-400">注册完成后回到原目标页面</div>
          </div>
          <div class="studio-metric">
            <div class="studio-metric-label">Deploy</div>
            <div class="studio-metric-value">前端独立</div>
            <div class="mt-2 text-sm text-slate-400">静态资源与构建产物归前端管理</div>
          </div>
          <div class="studio-metric">
            <div class="studio-metric-label">Experience</div>
            <div class="studio-metric-value">统一工作台</div>
            <div class="mt-2 text-sm text-slate-400">登录、注册与业务页视觉连续</div>
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

const router = useRouter();
const auth = useAuthStore();

const username = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');
const logoUrl = '/logo/ai-logo.png';

async function doRegister() {
  if (loading.value) {
    return;
  }

  errorMessage.value = '';
  loading.value = true;

  try {
    const data = await api.register({
      username: username.value.trim(),
      password: password.value
    });

    if (!data.success) {
      errorMessage.value = data.message || '注册失败';
      return;
    }

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
  const data = await auth.fetchCurrentUser();
  if (data && data.success) {
    await router.replace('/');
  }
});
</script>

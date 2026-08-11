<template>
  <div class="login-container">
    <div class="login-card">
      <h2>🛡️ Контроль обходов</h2>
      <div v-if="error" class="error-message">
        Неверное имя пользователя или пароль
      </div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Имя пользователя</label>
          <input
            type="text"
            id="username"
            v-model="username"
            placeholder="Введите логин"
            required
          />
        </div>
        <div class="form-group">
          <label for="password">Пароль</label>
          <input
            type="password"
            id="password"
            v-model="password"
            placeholder="Введите пароль"
            required
          />
        </div>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Вход...' : 'Войти' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../stores/auth'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'LoginView',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const username = ref('')
    const password = ref('')
    const error = ref(false)
    const loading = ref(false)

    const handleLogin = async () => {
      loading.value = true
      error.value = false

      try {
        await authStore.login(username.value, password.value)
        router.push('/')
      } catch (err) {
        error.value = true
      } finally {
        loading.value = false
      }
    }

    return {
      username,
      password,
      error,
      loading,
      handleLogin
    }
  }
}
</script>

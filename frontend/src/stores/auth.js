import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '../api/http'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')

  const isAuthenticated = computed(() => !!token.value)

  async function login(usernameInput, password) {
    try {
      const response = await http.post('/api/auth/login', {
        username: usernameInput,
        password: password
      })

      token.value = response.data.token
      username.value = response.data.username
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      return true
    } catch (error) {
      throw error
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  return {
    token,
    username,
    isAuthenticated,
    login,
    logout
  }
})

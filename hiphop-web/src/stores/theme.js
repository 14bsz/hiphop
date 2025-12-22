import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  const initTheme = () => {
    // Check localStorage or system preference
    const savedTheme = localStorage.getItem('hh-theme')
    // Default to dark mode if no preference is saved, or if system prefers dark
    // HipHop style is better in dark mode
    if (savedTheme === 'dark' || !savedTheme || window.matchMedia('(prefers-color-scheme: dark)').matches) {
      setDark(true)
    } else {
      setDark(false)
    }
  }

  const toggleTheme = () => {
    setDark(!isDark.value)
  }

  const setDark = (value) => {
    isDark.value = value
    const html = document.documentElement
    if (value) {
      html.classList.add('dark')
      localStorage.setItem('hh-theme', 'dark')
    } else {
      html.classList.remove('dark')
      localStorage.setItem('hh-theme', 'light')
    }
  }

  return {
    isDark,
    initTheme,
    toggleTheme
  }
})

import { onMounted, ref, watch } from 'vue'

type ThemeMode = 'dark' | 'light'

/**
 * Shared composable for staff page theme handling.
 *
 * Responsibilities:
 * - restore theme from localStorage
 * - toggle between dark and light
 * - keep <html data-theme="..."> synchronized
 */
export function useTheme(storageKey = 'staff-theme') {
    const theme = ref<ThemeMode>('dark')

    function toggleTheme(): void {
        theme.value = theme.value === 'dark' ? 'light' : 'dark'
        localStorage.setItem(storageKey, theme.value)
    }

    onMounted(() => {
        const savedTheme = localStorage.getItem(storageKey)

        if (savedTheme === 'light' || savedTheme === 'dark') {
            theme.value = savedTheme
        }
    })

    watch(
        theme,
        (value) => {
            document.documentElement.setAttribute('data-theme', value)
        },
        { immediate: true }
    )

    return {
        theme,
        toggleTheme,
    }
}
<template>
  <div class="flex items-center">
    <a
      href="https://ko-fi.com/gabylandais"
      target="_blank"
      rel="noopener noreferrer"
      :class="buttonClasses"
    >
      <!-- Ko-fi Heart Icon -->
      <svg
        class="w-4 h-4 mr-2"
        viewBox="0 0 24 24"
        fill="currentColor"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
        />
      </svg>
      <span class="font-medium">{{ buttonText }}</span>
    </a>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const buttonText = computed(() => t('common.support.footer_text', 'Payer un demi'))

const buttonClasses = computed(() => {
  return 'inline-flex items-center transition-all duration-200 bg-white text-pink-600 hover:text-pink-700 px-4 py-2 rounded-md shadow-sm hover:shadow border border-pink-200 hover:border-pink-300'
})
</script>

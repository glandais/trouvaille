<template>
  <div class="markdown-editor">
    <div class="editor-container">
      <Editor
        v-model="content"
        :plugins="plugins"
        :locale="locale"
        @change="handleChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Editor } from '@bytemd/vue-next'
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import 'bytemd/dist/index.css'
import 'highlight.js/styles/default.css'

interface Props {
  modelValue: string
  placeholder?: string
  maxLength?: number
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Écrivez votre description en markdown...',
  maxLength: 2000
})

const emit = defineEmits<Emits>()

const content = ref(props.modelValue)

const plugins = [
  gfm(),
  highlight()
]

const locale = {
  write: 'Écrire',
  preview: 'Aperçu',
  writeOnly: 'Écriture seule',
  previewOnly: 'Aperçu seul',
  exitWriteOnly: 'Quitter l\'écriture seule',
  exitPreviewOnly: 'Quitter l\'aperçu seul',
  exportPdf: 'Exporter en PDF',
  fullscreen: 'Plein écran',
  exitFullscreen: 'Quitter le plein écran',
  cheatsheet: 'Aide-mémoire',
  closeCheatsheet: 'Fermer l\'aide-mémoire'
}

const handleChange = (value: string) => {
  if (props.maxLength && value.length > props.maxLength) {
    value = value.substring(0, props.maxLength)
  }
  content.value = value
  emit('update:modelValue', value)
}

watch(() => props.modelValue, (newValue) => {
  if (newValue !== content.value) {
    content.value = newValue
  }
})
</script>

<style scoped>
.markdown-editor {
  @apply w-full;
}

.editor-container {
  @apply border border-gray-300 rounded-md overflow-hidden;
}

.editor-container :deep(.bytemd) {
  height: 400px;
}

.editor-container :deep(.bytemd-toolbar) {
  @apply bg-gray-50 border-b border-gray-200;
}

.editor-container :deep(.bytemd-editor) {
  @apply bg-white;
}

.editor-container :deep(.bytemd-preview) {
  @apply bg-gray-50;
}

.editor-container :deep(.bytemd-split) {
  @apply border-l border-gray-200;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .editor-container :deep(.bytemd-toolbar) {
    @apply bg-gray-800 border-gray-700;
  }
  
  .editor-container :deep(.bytemd-editor) {
    @apply bg-gray-900 text-white;
  }
  
  .editor-container :deep(.bytemd-preview) {
    @apply bg-gray-800 text-gray-100;
  }
  
  .editor-container :deep(.bytemd-split) {
    @apply border-gray-700;
  }
}
</style>
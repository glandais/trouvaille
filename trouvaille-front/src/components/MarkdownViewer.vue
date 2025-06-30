<template>
  <div class="markdown-viewer">
    <div ref="viewerElement" class="toastui-viewer-wrapper"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Editor, type EditorOptions } from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor-viewer.css'
import './MarkdownViewer.css'

interface Props {
  modelValue: string
  theme?: string
  linkAttributes?: Record<string, string>
  extendedAutolinks?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  theme: 'default',
  extendedAutolinks: true,
  linkAttributes: () => ({ target: '_blank', rel: 'noopener noreferrer' }),
})

const viewerElement = ref<HTMLElement>()
let viewer: Editor | null = null

const initViewer = () => {
  if (!viewerElement.value) return

  const options: EditorOptions = {
    el: viewerElement.value,
    viewer: true,
    initialValue: props.modelValue,
  }

  viewer = Editor.factory(options)
}

const destroyViewer = () => {
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
}

// Watch for changes to modelValue
watch(
  () => props.modelValue,
  (newValue) => {
    if (viewer && newValue !== undefined) {
      viewer.setMarkdown(newValue)
    }
  },
)

// Watch for theme changes
watch(
  () => props.theme,
  () => {
    // Recreate viewer with new theme
    destroyViewer()
    nextTick(() => {
      initViewer()
    })
  },
)

// Expose viewer methods for parent components
const setMarkdown = (markdown: string) => {
  if (viewer) {
    viewer.setMarkdown(markdown)
  }
}

defineExpose({
  setMarkdown,
  viewer: () => viewer,
})

onMounted(() => {
  nextTick(() => {
    initViewer()
  })
})

onUnmounted(() => {
  destroyViewer()
})
</script>


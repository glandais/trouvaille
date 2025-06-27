<template>
  <div class="markdown-viewer">
    <div ref="viewerElement" class="toastui-viewer-wrapper"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Editor, type EditorOptions } from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor-viewer.css'

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

<style scoped>
.markdown-viewer {
  @apply w-full;
}

.toastui-viewer-wrapper {
  @apply w-full;
}

/* Override Toast UI Viewer styles */
.toastui-viewer-wrapper :deep(.toastui-editor-contents) {
  @apply text-gray-900 leading-relaxed;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents h1) {
  @apply text-2xl font-bold mb-4 text-gray-900 border-b border-gray-200 pb-2;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents h2) {
  @apply text-xl font-bold mb-3 text-gray-900;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents h3) {
  @apply text-lg font-semibold mb-2 text-gray-900;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents h4),
.toastui-viewer-wrapper :deep(.toastui-editor-contents h5),
.toastui-viewer-wrapper :deep(.toastui-editor-contents h6) {
  @apply text-base font-semibold mb-2 text-gray-900;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents p) {
  @apply mb-4 text-gray-700;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents ul),
.toastui-viewer-wrapper :deep(.toastui-editor-contents ol) {
  @apply mb-4 ml-6;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents ul) {
  @apply list-disc;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents ol) {
  @apply list-decimal;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents li) {
  @apply mb-1 text-gray-700;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents blockquote) {
  @apply border-l-4 border-gray-300 pl-4 italic text-gray-600 mb-4 bg-gray-50 py-2;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents code) {
  @apply bg-gray-100 px-2 py-1 rounded text-sm font-mono text-gray-800;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents pre) {
  @apply bg-gray-100 p-4 rounded mb-4 overflow-x-auto;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents pre code) {
  @apply bg-transparent p-0 text-sm;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents a) {
  @apply text-blue-600 underline hover:text-blue-800;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents img) {
  @apply max-w-full h-auto rounded-lg shadow-sm;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents table) {
  @apply w-full border-collapse border border-gray-300 mb-4;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents th),
.toastui-viewer-wrapper :deep(.toastui-editor-contents td) {
  @apply border border-gray-300 px-3 py-2 text-left;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents th) {
  @apply bg-gray-50 font-semibold;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents hr) {
  @apply border-0 border-t border-gray-300 my-6;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents strong) {
  @apply font-bold;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents em) {
  @apply italic;
}

.toastui-viewer-wrapper :deep(.toastui-editor-contents del) {
  @apply line-through;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .toastui-viewer-wrapper :deep(.toastui-editor-contents) {
    @apply text-gray-100;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents h1),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents h2),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents h3),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents h4),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents h5),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents h6) {
    @apply text-gray-100;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents h1) {
    @apply border-gray-600;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents p),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents li) {
    @apply text-gray-300;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents blockquote) {
    @apply border-gray-600 text-gray-400 bg-gray-800;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents code) {
    @apply bg-gray-800 text-gray-200;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents pre) {
    @apply bg-gray-800;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents a) {
    @apply text-blue-400 hover:text-blue-300;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents table) {
    @apply border-gray-600;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents th),
  .toastui-viewer-wrapper :deep(.toastui-editor-contents td) {
    @apply border-gray-600;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents th) {
    @apply bg-gray-800;
  }

  .toastui-viewer-wrapper :deep(.toastui-editor-contents hr) {
    @apply border-gray-600;
  }
}
</style>

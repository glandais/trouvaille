<template>
  <div class="markdown-editor">
    <div class="editor-container">
      <div ref="editorElement" class="toastui-editor-wrapper"></div>
      <div v-if="maxLength" class="character-count">{{ currentLength }}/{{ maxLength }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Editor, type EditorOptions, type PreviewStyle, type EditorType } from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import './MarkdownEditor.css'

interface Props {
  modelValue: string
  placeholder?: string
  maxLength?: number
  height?: string
  initialEditType?: EditorType
  previewStyle?: PreviewStyle
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Écrivez votre description en markdown...',
  maxLength: 2000,
  height: '400px',
  initialEditType: 'wysiwyg',
  previewStyle: 'vertical',
})

const emit = defineEmits<Emits>()

const editorElement = ref<HTMLElement>()
const currentLength = ref(0)
let editor: Editor | null = null

const updateLength = (value: string) => {
  // Strip markdown formatting to count actual text length
  const textContent = value.replace(/[#*_`~\[\]()]/g, '').replace(/\n/g, ' ')
  currentLength.value = textContent.length
}

const initEditor = () => {
  if (!editorElement.value) return

  const options: EditorOptions = {
    el: editorElement.value,
    language: 'fr-FR',
    height: props.height,
    initialEditType: props.initialEditType,
    previewStyle: props.previewStyle,
    initialValue: props.modelValue,
    placeholder: props.placeholder,
    toolbarItems: [
      ['heading', 'bold', 'italic'],
      ['ul', 'ol', 'indent', 'outdent'],
      ['table', 'link'],
    ],
    events: {
      change: () => {
        if (editor) {
          const markdown = editor.getMarkdown()
          updateLength(markdown)

          if (props.maxLength && currentLength.value > props.maxLength) {
            // Don't update if exceeding max length
            return
          }

          emit('update:modelValue', markdown)
        }
      },
    },
    hooks: {
      addImageBlobHook: (blob: Blob | File, callback: (url: string, text?: string) => void) => {
        // For now, we'll create a blob URL. In a real app, you'd upload to a server
        const url = URL.createObjectURL(blob)
        const fileName = blob instanceof File ? blob.name : 'image'
        callback(url, fileName)
      },
    },
  }

  editor = new Editor(options)
}

const destroyEditor = () => {
  if (editor) {
    editor.destroy()
    editor = null
  }
}

// Watch for external changes to modelValue
watch(
  () => props.modelValue,
  (newValue) => {
    if (editor && editor.getMarkdown() !== newValue) {
      editor.setMarkdown(newValue, false)
      updateLength(newValue)
    }
  },
)

// Watch for height changes
watch(
  () => props.height,
  (newHeight) => {
    if (editor) {
      editor.setHeight(newHeight)
    }
  },
)

// Watch for preview style changes
watch(
  () => props.previewStyle,
  (newStyle) => {
    if (editor) {
      editor.changePreviewStyle(newStyle)
    }
  },
)

// Expose editor methods for parent components
const getMarkdown = () => {
  return editor?.getMarkdown() || ''
}

const getHTML = () => {
  return editor?.getHTML() || ''
}

const setMarkdown = (markdown: string, cursorToEnd = true) => {
  if (editor) {
    editor.setMarkdown(markdown, cursorToEnd)
  }
}

const insertText = (text: string) => {
  if (editor) {
    editor.insertText(text)
  }
}

const focus = () => {
  if (editor) {
    editor.focus()
  }
}

const blur = () => {
  if (editor) {
    editor.blur()
  }
}

// Expose methods for parent components
defineExpose({
  getMarkdown,
  getHTML,
  setMarkdown,
  insertText,
  focus,
  blur,
  editor: () => editor,
})

onMounted(() => {
  nextTick(() => {
    initEditor()
  })
})

onUnmounted(() => {
  destroyEditor()
})
</script>


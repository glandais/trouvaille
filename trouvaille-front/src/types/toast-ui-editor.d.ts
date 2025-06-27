declare module '@toast-ui/editor/dist/toastui-editor-viewer' {
  export class Viewer {
    constructor(options: any)
    setMarkdown(markdown: string): void
    on(type: string, handler: (...args: any[]) => void): void
    off(type: string): void
    destroy(): void
  }
}

declare module '@toast-ui/editor' {
  export type PreviewStyle = 'tab' | 'vertical'
  export type EditorType = 'markdown' | 'wysiwyg'

  export interface EventMap {
    load?: (param: Editor) => void
    change?: (editorType: EditorType) => void
    caretChange?: (editorType: EditorType) => void
    focus?: (editorType: EditorType) => void
    blur?: (editorType: EditorType) => void
    keydown?: (editorType: EditorType, ev: KeyboardEvent) => void
    keyup?: (editorType: EditorType, ev: KeyboardEvent) => void
    beforePreviewRender?: (html: string) => string
    beforeConvertWysiwygToMarkdown?: (markdownText: string) => string
  }

  export type HookMap = {
    addImageBlobHook?: (blob: Blob | File, callback: (url: string, text?: string) => void) => void
  }

  export interface EditorOptions {
    el: HTMLElement
    height?: string
    minHeight?: string
    initialValue?: string
    previewStyle?: PreviewStyle
    initialEditType?: EditorType
    events?: EventMap
    hooks?: HookMap
    language?: string
    useCommandShortcut?: boolean
    usageStatistics?: boolean
    toolbarItems?: (string | any)[][]
    hideModeSwitch?: boolean
    plugins?: any[]
    extendedAutolinks?: boolean
    placeholder?: string
    linkAttributes?: any
    customHTMLRenderer?: any
    customMarkdownRenderer?: any
    referenceDefinition?: boolean
    customHTMLSanitizer?: any
    previewHighlight?: boolean
    frontMatter?: boolean
    widgetRules?: any[]
    theme?: string
    autofocus?: boolean
    viewer?: boolean
  }

  export class Editor {
    static factory(options: EditorOptions): Editor | null
    constructor(options: EditorOptions)
    
    changePreviewStyle(style: PreviewStyle): void
    focus(): void
    blur(): void
    setMarkdown(markdown: string, cursorToEnd?: boolean): void
    setHTML(html: string, cursorToEnd?: boolean): void
    getMarkdown(): string
    getHTML(): string
    insertText(text: string): void
    setHeight(height: string): void
    destroy(): void
    on(type: string, handler: (...args: any[]) => void): void
    off(type: string): void
    addHook(type: string, handler: (...args: any[]) => void): void
    removeHook(type: string): void
  }

  export interface ViewerOptions {
    el: HTMLElement
    initialValue?: string
    events?: any
    plugins?: any[]
    extendedAutolinks?: boolean
    linkAttributes?: any
    customHTMLRenderer?: any
    referenceDefinition?: boolean
    customHTMLSanitizer?: any
    frontMatter?: boolean
    usageStatistics?: boolean
    theme?: string
  }

  export class ToastUIEditorViewer {
    constructor(options: ViewerOptions)
    setMarkdown(markdown: string): void
    on(type: string, handler: (...args: any[]) => void): void
    off(type: string): void
    destroy(): void
  }

  export default Editor
}
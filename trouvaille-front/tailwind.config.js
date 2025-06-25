/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      aspectRatio: {
        '4/3': '4 / 3',
        '16/9': '16 / 9',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
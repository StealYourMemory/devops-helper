import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia';

import hljs from 'highlight.js';
import hljsVuePlugin from "@highlightjs/vue-plugin";
import sql from "highlight.js/lib/languages/sql";
import xml from "highlight.js/lib/languages/xml";
import html from "highlight.js/lib/languages/vbscript-html";
import json from "highlight.js/lib/languages/json";
import yaml from "highlight.js/lib/languages/yaml";
import shell from "highlight.js/lib/languages/shell";
hljs.registerLanguage("yaml", yaml);
hljs.registerLanguage("json", json);
hljs.registerLanguage("sql", sql);
hljs.registerLanguage("xml", xml);
hljs.registerLanguage("html", html);
hljs.registerLanguage("sh", shell);

const app = createApp(App)
app.use(router)
//全局引入element-plus
app.use(ElementPlus)
app.use(createPinia());
app.use(hljsVuePlugin);
//全局引入element-plus icon
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.mount('#app')

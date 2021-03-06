(ns sitecompiler.system
  (:require [sitecompiler.config :as conf]
            [sitecompiler.reader :as rd]
            [sitecompiler.mdparser :as md]
            [sitecompiler.htmlparser :as html]
            [sitecompiler.hcparser :as hcp]
            [sitecompiler.mustache :as mst]
            [sitecompiler.hiccup :as hc]
            [sitecompiler.tagsgen :as tg]
            [sitecompiler.pagesgen :as pg]))

(defn system [& [cfg-file]]
  (let [config (conf/new-config (or cfg-file "config.clj"))

        ;; Парсеры, которые не зависят от input-reader-а,
        ;; могут быть использованы для обработки входных данных
        mdparser (md/new-md-parser)
        htmlparser (html/new-html-parser)
        hcparser (hcp/new-hc-parser)

        ;; Reader читает все входные файлы и преобразует их
        ;; в "сырой" html
        reader (rd/new-input-reader
                config
                [htmlparser
                 hcparser
                 mdparser])

        ;; Файлы, которые будем генерить
        input-files (:input-files reader)

        ;; Шаблоны для файлов
        templates (:templates reader)

        ;; Обработчики шаблонов (Mustache).
        mstrenderer (mst/new-mst-renderer)
        hcrenderer (hc/new-hc-renderer)
        

        ;; Собрать нужные тэги
        tags-chunks (tg/chunks config
                               input-files)

        ;; Сгенерировать списки страниц по тегам
        tags-lists (tg/generate config
                                templates
                                tags-chunks
                                [mstrenderer
                                 hcrenderer])

        ;; Сгенерить одиночные страницы по тегам
        tags-pages (pg/generate-tags-pages config
                                           templates
                                           input-files
                                           [mstrenderer
                                            hcrenderer])

        ;; Сгенерить одиночные страницы без тегов
        single-pages (pg/generate-single-pages config
                                               templates
                                               input-files
                                               [mstrenderer
                                                hcrenderer])
        ]
    {:config config
     :tags-lists tags-lists
     :tags-pages tags-pages
     :input-files input-files
     :single-pages single-pages
     } ))


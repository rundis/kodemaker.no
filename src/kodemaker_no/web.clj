(ns kodemaker-no.web
  (:require clojure.core.memoize
            [clojure.data.json :as json]
            [config :refer [export-directory]]
            [imagine.core :as imagine]
            [kodemaker-no.content :refer [load-content]]
            [kodemaker-no.cultivate :refer [cultivate-content]]
            [kodemaker-no.homeless :refer [wrap-content-type-utf-8]]
            [kodemaker-no.html5-walker :as html5-walker]
            [kodemaker-no.pages :as pages]
            [kodemaker-no.prepare-pages :refer [prepare-pages]]
            [kodemaker-no.validate :refer [validate-content]]
            [optimus-img-transform.core :refer [transform-images]]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [prone.middleware :as prone]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [stasis.core :as stasis]))

(defn get-assets []
  (assets/load-assets
   "public"
   [
    ;: ny
    "/css/kodemaker.css"
    #"/img/.*\..+"
    #"/fonts/.*\..+"
    #"/icons/.*\..+"

    ;; gammal
    #"/styles/.*\.css"
    "/favicon.ico"
    #"/certificates/.*\.pdf"
    #"/illustrations/.*\.jpg"
    #"/illustrations/.*\.png"
    #"/thumbs/.*\.jpg"
    #"/forside/.*\.jpg"
    #"/references/.*\.jpg"
    #"/fullsize-photos/.*\.jpg"
    #"/photos/.*\.jpg"
    #"/photos/.*\.svg"
    #"/photos/.*\.png"
    #"/logos/.*\.png"
    #"/logos/.*\.svg"
    #"/images/.*\.png"
    #"/images/blogg/.*\.png"
    #"/videos/.*\.mp4"]))

(def image-asset-config
  {:prefix "image-assets"
   :resource-path "public"
   :transformations
   {:vcard {:transformations [[:crop {:preset :square}]]
            :retina-optimized? true
            :width 92}}})

(defn get-pages []
  (let [content (load-content)
        pages (-> content
                  validate-content
                  cultivate-content
                  pages/create-pages
                  (prepare-pages image-asset-config))]
    (stasis/merge-page-sources
     {:site-pages pages
      :raw-pages (:raw-pages content)
      :raw-css (:raw-css content)})))

(def optimize
  (-> (fn [assets options]
        (-> (map #(assoc % :context-path "/assets") assets)
            (transform-images {:regexp #"/fullsize-photos/.*\.jpg"
                               :quality 0.3
                               :width (* 920 2)
                               :progressive true})
            (transform-images {:regexp #"/photos/.*\.jpg"
                               :quality 0.3
                               :width (* 290 2)
                               :progressive true})
            (transform-images {:regexp #"/references/.*\.jpg"
                               :quality 0.3
                               :width (* 680 2)
                               :progressive true})
            (transform-images {:regexp #"/illustrations/.*\.jpg"
                               :quality 0.3
                               :width (* 210 2)
                               :progressive true})
            (transform-images {:regexp #"/thumbs/.*\.jpg"
                               :quality 0.3
                               :width (* 100 2)
                               :progressive false}) ; too small, will be > kb
            (optimizations/all options)
            (->> (remove :bundled)
                 (remove :outdated))))
      (clojure.core.memoize/lru {} :lru/threshold 3)))

(defn- dummy-mail-sender [handler]
  (fn [req]
    (if (= "/send-mail" (:uri req))
      (do (prn "Sending mail!" (slurp (:body req)))
          {:status 302
           :headers {"Location" "/takk/"}})
      (handler req))))

(def app (-> (stasis/serve-pages get-pages)
             dummy-mail-sender
             (wrap-resource "videos")
             (imagine/wrap-images image-asset-config)
             (optimus/wrap get-assets optimize serve-live-assets)
             wrap-content-type
             wrap-content-type-utf-8
             prone/wrap-exceptions))

(defn extract-images [html]
  (for [node (html5-walker/find html [:img])]
    (.getAttribute node "src")))

(defn get-images [pages-dir]
  (->> (stasis/slurp-directory pages-dir #"\.html+$")
       vals
       (mapcat extract-images)
       (into #{})))

(defn get-image-assets [pages-dir asset-config]
  (->> (get-images pages-dir)
       (filter #(imagine/image-url? % asset-config))))

(defn export-images [pages-dir dir asset-config]
  (doseq [image (get-image-assets pages-dir asset-config)]
    (-> image
        imagine/image-spec
        (imagine/inflate-spec asset-config)
        (imagine/transform-image-to-file (str dir image)))))

(defn- load-export-dir []
  (stasis/slurp-directory export-directory #"\.[^.]+$"))

(defn export [& args]
  (let [[format] (map read-string args)
        assets (optimize (get-assets) {})
        old-files (load-export-dir)]
    (stasis/empty-directory! export-directory)
    (optimus.export/save-assets assets export-directory)
    (stasis/export-pages (get-pages) export-directory {:optimus-assets assets
                                                       :base-url "https://www.kodemaker.no"})
    (export-images export-directory export-directory (assoc image-asset-config :cacheable-urls? true))
    (if (= format :json)
      (println (json/write-str (dissoc (stasis/diff-maps old-files (load-export-dir)) :unchanged)))
      (do
        (println)
        (println "Export complete:")
        (stasis/report-differences old-files (load-export-dir))
        (println)))))

(comment
  (export-images "./build/" "./build/" image-asset-config)

  (get-image-assets "./build/" image-asset-config)


  )

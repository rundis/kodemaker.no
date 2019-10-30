(ns kodemaker-no.ingestion.video
  (:require [kodemaker-no.formatting :as f]
            [kodemaker-no.homeless :as h]))

(defn find-video [^String url]
  (when url
    (cond
      (.startsWith url "http://www.youtube.com/watch?v=") {:type :youtube, :id (subs url 31)}
      (.startsWith url "http://vimeo.com/album/") {:type :vimeo, :id (second (re-find #"http://vimeo.com/album/\d+/video/(\d+)" url))}
      (re-find #"https?://vimeo.com/\d+" url) {:type :vimeo, :id (second (re-find #"https?://vimeo.com/(\d+)" url))}
      (re-find #"https?://vimeo.com/user\d+/review/\d+/\S+" url) {:type :vimeo, :id (second (re-find #"https?://vimeo.com/user\d+/review/(\d+)/\S+" url))}
      :else nil)))

(defn- create-embed-code [url]
  (let [{:keys [type id]} (find-video url)]
    (case type
      :youtube [:div.video-embed
                [:iframe {:src (str "//www.youtube.com/embed/" id)
                          :frameborder "0"
                          :allowfullscreen true}]]
      :vimeo [:div.video-embed
              [:iframe {:src (str "//player.vimeo.com/video/" id "?title=0&amp;byline=0&amp;portrait=0")
                        :frameborder "0"
                        :allowfullscreen true}]]
      nil)))

(defn- create-video-page-for-presentation? [presentation]
  (and (not (:presentation/direct-link? presentation))
       (find-video (:presentation/video-url presentation))))

(defn- video-id [presentation]
  (some-> presentation
          :presentation/title
          f/to-id-str
          keyword))

(defn- video-url [presentation]
  (if (create-video-page-for-presentation? presentation)
    (str "/" (name (video-id presentation)) "/")
    (:presentation/video-url presentation)))

(def video-keys
  {:title :video/title
   :blurb :video/blurb
   :date :video/date
   :embed-code :video/embed-code
   :direct-link? :video/direct-link?})

(defn video-data [person-ident presentation]
  (when-let [url (video-url presentation)]
    (cond-> (h/select-renamed-keys presentation video-keys)
      (create-video-page-for-presentation? presentation)
      (merge {:page/uri url
              :page/kind :video})

      :always (merge {:db/ident (video-id presentation)
                      :video/by [{:db/ident person-ident}]
                      :video/url url}))))
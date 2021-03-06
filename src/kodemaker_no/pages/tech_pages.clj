(ns kodemaker-no.pages.tech-pages
  (:require [clj-time.core :as t]
            [kodemaker-no.date :as d]
            [kodemaker-no.formatting :as f]
            [kodemaker-no.markup :as markup]))

(defn- render-recommendation [{:keys [title by blurb link]}]
  (list [:h3 [:a {:href (:url link)} title]]
        [:p.near.cookie-w [:span.cookie "Anbefalt av " (f/comma-separated (map f/link-to-person by))]]
        (markup/append-to-paragraph
         (f/to-html blurb)
         (list " " (markup/render-link link)))))

(defn- render-recommendations [recommendations _]
  (list [:h2.mhn "Våre anbefalinger"]
        (map render-recommendation recommendations)))

(defn- render-presentation [{:keys [urls title thumb by blurb]}]
  (list
   [:h3.mtn [:a {:href (or (:video urls) (:slides urls) (:source urls))} title]]
   [:p.near.cookie-w [:span.cookie "Holdt av " (f/comma-separated (map f/link-to-person by))]]
   [:p blurb
    (when-let [url (:video urls)] (list " " [:a.nowrap {:href url} "Se video"]))
    (when-let [url (:slides urls)] (list " " [:a.nowrap {:href url} "Se slides"]))
    (when-let [url (:source urls)] (list " " [:a.nowrap {:href url} "Se koden"]))]))

(defn- render-presentations [presentations _]
  (list [:h2.mhn "Våre presentasjoner"]
        (map render-presentation presentations)))

(defn- render-blog-post [{:keys [title by blurb url]}]
  (list
   [:h3 [:a {:href url} title]]
   [:p.near.cookie-w [:span.cookie "Skrevet av " (f/link-to-person by)]]
   (markup/append-to-paragraph
    (f/to-html blurb)
    (list " " [:a {:href url} "Les posten"]))))

(defn- render-blog-posts [posts _]
  (list [:h2.mhn "Våre bloggposter"]
        (map render-blog-post posts)))

(defn- render-side-project [{:keys [title description link illustration by]}]
  [:div.bd
   [:h3.mtn [:a {:href (:url link)} title]]
   [:p.near.cookie-w [:span.cookie "Av " (f/comma-separated (map f/link-to-person by))]]
   (-> (f/to-html description)
       (markup/append-to-paragraph
        (list " " (markup/render-link link)))
       (markup/prepend-to-paragraph
        [:a.illu {:href (:url link)} [:img {:src illustration}]]))])

(defn- render-side-projects [projects _]
  (list [:h2.mhn "Sideprosjekter"]
        (map render-side-project projects)))

(defn- render-open-source-project [project]
  (list (f/comma-separated (map f/link-to-person (:by project)))
        " utviklet "
        [:a {:href (:url project)} (:name project)]
        ". "
        (markup/strip-paragraph (f/to-html (:description project)))))

(defn- render-open-source-projects [projects _]
  (list [:h2.mhn "Open source"]
        (if (next projects)
          [:ul
           (map (fn [p] [:li (render-open-source-project p)]) projects)]
          [:p (render-open-source-project (first projects))])))

(defn- render-upcoming-event [now {:keys [by title date url call-to-action location description]}]
  (list [:h3 title]
        [:p description]
        [:p (list (f/comma-separated (map f/link-to-person by))
                  ", "
                  [:a {:href (:url location)} (:title location)]
                  ", "
                  (d/clever-date date now)
                  (if call-to-action
                    (list " - "
                          [:a {:href (:url call-to-action)} (:text call-to-action)])))]))

(defn- render-upcoming [events tech]
  (let [date (t/today)
        upcoming (filter #(d/within? date (d/in-weeks date 6) (:date %)) events)]
    (when (seq upcoming)
      (list [:h2.mhn (str "Våre kommende foredrag/kurs om " (:name tech))]
            (->> upcoming
                 (sort-by :date)
                 (map (partial render-upcoming-event date)))))))

(defn render-ad [{:keys [heading blurb link-text]} _]
  (list
   [:h4 heading]
   [:p blurb " " [:a.nowrap {:href "/skjema/"} link-text]]))

(defn- maybe-include [tech kw f]
  (when (kw tech)
    (f (kw tech) tech)))

(defn- tech-page [tech]
  {:title (:name tech)
   :illustration (:illustration tech)
   :site (:site tech)
   :aside (maybe-include tech :ad render-ad)
   :lead (f/to-html (:description tech))
   :body (list
          (maybe-include tech :upcoming render-upcoming)
          (maybe-include tech :recommendations render-recommendations)
          (maybe-include tech :blog-posts render-blog-posts)
          (maybe-include tech :presentations render-presentations)
          (maybe-include tech :side-projects render-side-projects)
          (maybe-include tech :open-source-projects render-open-source-projects))})

(defn tech-pages [techs]
  (into {} (map (juxt :url #(partial tech-page %)) techs)))

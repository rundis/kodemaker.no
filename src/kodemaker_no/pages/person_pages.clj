(ns kodemaker-no.pages.person-pages
  (:require [clj-time.core :as time]
            [kodemaker-no.date :as d]
            [kodemaker-no.formatting :as f]
            [kodemaker-no.markup :as markup]))

(defn- render-recommendation [{:keys [title tech blurb link]}]
  (list [:h3 [:a {:href (:url link)} title]]
        (f/render-tech-bubble tech)
        (markup/append-to-paragraph
         (f/to-html blurb)
         (list " " (markup/render-link link)))))

(defn- render-recommendations [recs person]
  (list [:h2.mhn (str (:genitive person) " anbefalinger")]
        (map render-recommendation (take 3 recs))))

(defn- render-hobby [{:keys [title description url illustration]}]
  [:div.bd
   [:h3.mtn title]
   (markup/prepend-to-paragraph
    (f/to-html description)
    (if url
      [:a.illu {:href url} [:img {:src illustration}]]
      [:img.illu {:src illustration}]))])

(defn- render-hobbies [hobbies _]
  (list [:h2.mhn "Snakker gjerne om"]
        (map render-hobby hobbies)))

(defn- render-screencast [{:keys [title description url illustration tech]}]
  [:div.bd
   [:h3.mtn [:a {:href url} title]]
   (f/render-tech-bubble tech)
   (-> (f/to-html description)
       (markup/append-to-paragraph
        (list " " (markup/render-link {:url url :text "Se videoene"}) "."))
       (markup/prepend-to-paragraph
        [:a.illu {:href url} [:img {:src illustration}]]))])

(defn- render-screencasts [screencasts _]
  (list [:h2.mhn "Screencasts"]
        (map render-screencast screencasts)))

(defn- render-side-project [{:keys [title description link illustration tech]}]
  [:div.bd
   [:h3.mtn [:a {:href (:url link)} title]]
   (f/render-tech-bubble tech)
   (-> (f/to-html description)
       (markup/append-to-paragraph
        (list " " (markup/render-link link)))
       (markup/prepend-to-paragraph
        [:a.illu {:href (:url link)} [:img {:src illustration}]]))])

(defn- render-side-projects [side-projects _]
  (list [:h2.mhn "Sideprosjekter"]
        (map render-side-project side-projects)))

(defn- inline-list [label nodes]
  (list [:strong label]
        (f/comma-separated nodes)
        "<br>"))

(defn- render-tech [{:keys [using-at-work favorites-at-the-moment want-to-learn-more]} _]
  [:p
   (when favorites-at-the-moment
     (inline-list "Favoritter for tiden: " (map markup/link-if-url favorites-at-the-moment)))
   (when using-at-work
     (inline-list "Bruker på jobben: " (map markup/link-if-url using-at-work)))
   (when want-to-learn-more
     (inline-list "Vil lære mer: " (map markup/link-if-url want-to-learn-more)))])

(defn- render-presentation [{:keys [urls title thumb blurb tech]}]
  (list [:h3.mtn [:a {:href (or (:video urls) (:slides urls) (:source urls))} title]]
        (f/render-tech-bubble tech)
        [:p blurb
         (when-let [url (:video urls)] (list " " [:a.nowrap {:href url} "Se video"]))
         (when-let [url (:slides urls)] (list " " [:a.nowrap {:href url} "Se slides"]))
         (when-let [url (:source urls)] (list " " [:a.nowrap {:href url} "Se koden"]))]))

(defn- render-presentations [presentations person]
  (list [:h2.mhn (str (:genitive person) " foredrag")]
        (map render-presentation presentations)))

(defn- render-endorsement [{:keys [photo author title project quote]}]
  [:div.media
   (when photo [:img.img.thumb.mts {:src photo}])
   [:div.bd
    [:h3.mtn author]
    (when (or title project)
      [:p.near.mbl
       [:strong
        (when title title)
        (when (and title project) ", ")
        (when project (:name project))]])
    [:p [:q quote]]]])

(defn- render-endorsements [endorsements person]
  (list [:h2.mhn (str (:genitive person) " referanser")]
        (interpose [:hr.mtn.mhn] (map render-endorsement endorsements))))

(def cv
  {:id :cv             :baseUrl "/cv/"  :title "Cv"})

(def presence-items
  [cv
   {:id :twitter       :baseUrl "https://twitter.com/"      :title "Twitter"}
   {:id :linkedin      :baseUrl "https://www.linkedin.com"      :title "LinkedIn"}
   {:id :stackoverflow :baseUrl "https://stackoverflow.com/" :title "StackOverflow"}
   {:id :github        :baseUrl "https://github.com/"           :title "GitHub"}
   {:id :coderwall     :baseUrl "https://coderwall.com/"    :title "Coderwall"}])

(defn- render-presence-item [item presence]
  (when-let [nick (-> item :id presence)]
    [:a {:href (str (:baseUrl item) nick "/")
         :class (str "presence " (name (:id item)))
         :title (:title item)}]))

(defn render-presence [presence]
  [:div.mod
   (keep #(render-presence-item % presence) presence-items)])

(defn- render-aside [{:keys [full-name title phone-number email-address presence photos]}]
  (list
   [:div.illustration.mbn
    [:img {:src (:half-figure photos)}]]
   [:div.tight
    [:hr.mtn.s-3of4]
    [:h4 full-name]
    [:p
     title "<br>"
     [:span.nowrap phone-number] "<br>"
     [:a {:href (str "mailto:" email-address)} email-address]]
    (when (seq presence) (render-presence presence))]))

(defn- render-blog-post [{:keys [title tech blurb url]}]
  (list
   [:h3 [:a {:href url} title]]
   (f/render-tech-bubble tech)
   (markup/append-to-paragraph
    (f/to-html blurb)
    (list " " [:a {:href url} "Les posten"]))))

(defn- render-blog-posts [posts person]
  (list
   [:h2.mhn (str (:genitive person) " bloggposter")]
   (map render-blog-post (take 3 posts))))

(defn- render-project [{:keys [customer years tech description]}]
  (list
   [:h3 customer " " [:span.tiny.shy (f/year-range years)]]
   (f/render-tech-bubble (take 5 tech))
   (f/to-html description)))

(defn- render-projects [projects person]
  (list
   [:h2.mhn "Prosjekter"]
   (->> projects
        (remove :exclude-from-profile?)
        (filter :description)
        (map render-project))
   (when-let [nick (-> person :presence :cv)]
     [:p [:a {:href (str (:baseUrl cv) nick "/")}
          "Se flere prosjekter i " (:genitive person) " CV"]])))

(defn- should-split-open-source-by-tech [projects contributions]
  (let [entries (concat projects contributions)]
    (and (< 6 (count entries))
         (< 1 (count (distinct (map #(first (:tech %)) entries)))))))

(defn- render-open-source-contributions [contributions]
  (when (seq contributions)
    (list "Har bidratt til " (f/comma-separated (map markup/link-if-url contributions)) ".")))

(defn- render-extra-open-source-projects [projects]
  (when (seq projects)
    (list "Har også utviklet " (f/comma-separated (map markup/link-if-url projects)) ".")))

(defn- render-open-source-project [project]
  (when project
    (list "Utviklet "
          (markup/link-if-url project) ". "
          (markup/strip-paragraph (f/to-html (:description project))))))

(defn- render-open-source-entries [projects contributions]
  (let [entries (->> (conj (mapv render-open-source-project (take 5 projects))
                           (render-extra-open-source-projects (drop 5 projects))
                           (render-open-source-contributions contributions))
                     (remove nil?))]
    (if (next entries)
      [:ul (map (fn [node] [:li node]) entries)]
      [:p (first entries)])))

(defn- render-split-open-source [projects contributions]
  (->> (concat projects contributions)
       (map (comp first :tech))
       distinct
       (map (fn [tech]
              (list [:h3 (markup/link-if-url tech)]
                    (render-open-source-entries
                     (filter #(= tech (first (:tech %))) projects)
                     (filter #(= tech (first (:tech %))) contributions)))))))

(defn- render-open-source [{:keys [open-source-projects open-source-contributions]}]
  (list
   [:h2.mhn "Open source"]
   (if (should-split-open-source-by-tech open-source-projects open-source-contributions)
     (render-split-open-source open-source-projects open-source-contributions)
     (render-open-source-entries open-source-projects open-source-contributions))))

(defn- maybe-include-open-source [person]
  (when (or (:open-source-projects person)
            (:open-source-contributions person))
    (render-open-source person)))

(defn- maybe-include [person kw f]
  (when (kw person)
    (f (kw person) person)))

(defn- render-upcoming-event [now {:keys [title tech date url call-to-action location description]}]
  (list [:h3 [:a {:href url} title]]
        (f/render-tech-bubble tech)
        [:p description]
        [:p (list [:a {:href (:url location)} (:title location)]
                  ", "
                  (d/clever-date date now)
                  (when call-to-action
                    (list " - "
                          [:a {:href (:url call-to-action)} (:text call-to-action)])))]))

(defn- render-upcoming [events person]
  (let [date (time/today)
        upcoming (filter #(d/within? date (d/in-weeks date 12) (:date %)) events)]
    (when (seq upcoming)
      (list [:h2.mhn (str (:genitive person) " kommende foredrag/kurs")]
            (->> upcoming
                 (sort-by :date)
                 (map (partial render-upcoming-event date)))))))

(defn- person-page [person]
  {:title {:head (:full-name person)
           :h1 (:full-name person)
           :arrow (:next-person-url person)}
   :lead (f/to-html (:description person))
   :aside (render-aside person)
   :body (list
          (maybe-include person :tech render-tech)
          (maybe-include person :upcoming render-upcoming)
          (maybe-include person :recommendations render-recommendations)
          (maybe-include person :hobbies render-hobbies)
          (maybe-include person :screencasts render-screencasts)
          (maybe-include person :side-projects render-side-projects)
          (maybe-include person :blog-posts render-blog-posts)
          (maybe-include person :presentations render-presentations)
          (maybe-include-open-source person)
          (maybe-include person :projects render-projects)
          (maybe-include person :endorsements render-endorsements))})

(defn person-pages [people]
  (->> people
       (remove #(not (get % :profile-active? true)))
       (map (juxt :url #(partial person-page %)))
       (into {})))

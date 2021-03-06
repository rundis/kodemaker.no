(ns kodemaker-no.new-pages.cv-page
  (:require [datomic-type-extensions.api :as d]
            [kodemaker-no.formatting :as f]
            [kodemaker-no.homeless :as h]
            [kodemaker-no.markup :as m]
            [kodemaker-no.new-pages.person :as person]
            [ui.elements :as e])
  (:import java.time.format.DateTimeFormatter))

(defn years-of-experience [{:person/keys [experience-since]}]
  (let [years (when experience-since
                (- (+ 1900 (.getYear (java.util.Date.))) experience-since))]
    (cond
      (nil? years) "lite"
      (<= years 30) (str years " års")
      :default "mange års")))

(defn endorsement-highlight [{:person/keys [endorsement-highlight]}]
  (when-let [{:keys [quote author title]} endorsement-highlight]
    {:text quote
     :source (str author (when title (str ", " title)))}))

(defn project-highlight [{:keys [blurb customer link]}]
  {:title customer
   :text blurb
   :href link})

(defn cv-profile [cv]
  (d/entity (d/entity-db cv) (:cv/person cv)))

(def tech-labels
  {:proglang "Programmeringsspråk"
   :devtools "Utviklingsverktøy"
   :vcs "Versjonskontroll"
   :methodology "Metodikk"
   :os "Operativsystem"
   :database "Database"
   :devops "Devops"
   :cloud "Skytjenester"
   :security "Sikkerhet"
   :tool "Verktøy"
   :frontend "Frontend"
   :cv/other "Annet"})

(def tech-order
  [:proglang :devtools :vcs :methodology :os :database :devops :cloud :security :tool :frontend :cv/other])

(defn side-project-techs [person]
  (mapcat :side-project/techs (:person/side-projects person)))

(defn blog-post-techs [person]
  (mapcat :blog-post/techs (:blog-post/_author person)))

(defn screencast-techs [person]
  (mapcat :screencast/techs (:person/screencasts person)))

(defn presentation-techs [person]
  (mapcat :presentation/techs (:person/presentations person)))

(defn business-presentation-techs [person]
  (mapcat :presentation-product/techs (:person/business-presentations person)))

(defn open-source-techs [person]
  (->> (concat (:person/open-source-projects person)
               (:person/open-source-contributions person))
       (mapcat :tech)))

(defn project-techs [person]
  (mapcat :project/techs (:person/projects person)))

(defn all-techs [db cv person]
  (->> (concat (:person/using-at-work person)
               (:person/innate-skills person)
               (side-project-techs person)
               (blog-post-techs person)
               (screencast-techs person)
               (presentation-techs person)
               (open-source-techs person)
               (project-techs person))
       (remove (or (:person/exclude-techs person) #{}))
       frequencies
       (sort-by (comp - second))
       (map first)
       (person/prefer-techs (person/preferred-techs person))
       (map #(d/entity db %))
       (group-by :tech/type)))

(comment
  (def conn (d/connect "datomic:mem://kodemaker"))
  (def db (d/db conn))
  (def person (d/entity db :person/christian))
  (def cv (:cv/_person person))

  (:person/profile-overview-picture person)
  (:person/profile-page-picture person)
  (:person/cv-picture person)
  (:person/profile-pictures person)

  (->> (:person/certifications person)
       (group-by :year)
       (sort-by (comp - first)))

  (->> (:person/projects person)
       (sort-by :list/idx)
       first
       :project/employer
       (d/entity db)
       :employer/name)

  (into {}  (first (:person/projects person)))

  (all-techs db cv person))

(defn- prep-tech [all-techs tech-type]
  (when-let [techs (get all-techs tech-type)]
    {:title (get tech-labels tech-type)
     :contents [[:p.text (e/enumerate-techs techs)]]}))

(defn technology-section [cv person]
  (let [techs (all-techs (d/entity-db cv) cv person)
        other-techs (seq (mapcat identity (vals (apply dissoc techs tech-order))))
        techs (assoc techs :cv/other other-techs)]
    {:kind :definitions
     :title "Teknologi"
     :id "technology"
     :definitions (->> tech-order
                       (map #(prep-tech techs %))
                       (filter identity))}))

(defn format-year-month [date]
  (when date
    (.format (DateTimeFormatter/ofPattern "MM.yyyy") date)))

(defn- year-range [years & [start end]]
  (if start
    (str (format-year-month start) " - " (format-year-month end))
    (f/year-range years)))

(defn- project-year-range [project]
  (year-range (:project/years project) (:project/start project) (:project/end project)))

(defn render-project [project]
  {:title (list (or (:cv/customer project)
                    (:project/customer project))
                [:br] (project-year-range project))
   :contents [[:h4.h6 [:em (:project/summary project)]]
              [:div.text
               (f/to-html (or (:cv/description project) (:project/description project)))]
              [:p.text-s.annotation.mts
               (->> (h/unwrap-idents project :project/techs)
                    (map :tech/name)
                    e/comma-separated)]]})

(defn render-employer [employer employment projects]
  (into
   [{:type :separator
     :category "Arbeidsgiver"
     :title (:employer/name employer)
     :description (some-> employment :description f/to-html)}]
   (map render-project projects)))

(defn employment [person employer]
  (get-in person [:person/employments (some-> employer name keyword)]))

(defn projects-section [cv person]
  (let [db (d/entity-db person)]
    {:kind :definitions
     :title "Prosjekter"
     :id "projects"
     :definitions
     (->> (:person/projects person)
          (sort-by :list/idx)
          (group-by :project/employer)
          (mapcat (fn [[employer projects]]
                    (render-employer (d/entity db employer) (employment person employer) projects))))}))

(defn endorsements-section [cv person]
  (when-let [endorsements (seq (sort-by :list/idx (:person/endorsements person)))]
    {:kind :definitions
     :id "endorsements"
     :title "Anbefalinger"
     :definitions (->> endorsements
                       (map (fn [endorsement]
                              {:type :complex-title
                               :title [:div
                                       [:h3.h4-light (:author endorsement)]
                                       [:p (:title endorsement)]]
                               :contents [(e/blockquote endorsement)]})))}))

(defn render-certifications [[year certifications]]
  {:title (str year)
   :contents
   [[:ul.dotted.dotted-tight
     (map (fn [{:keys [name year institution url certificate]}]
            [:li
             (if url
               [:a.link {:href url} name]
               name)
             (when institution
               (format " (%s)" institution))
             (when certificate
               (list " - " [:a.link {:href (:url certificate)} (or (:text certificate) "Kursbevis")]))])
          certifications)]]})

(defn certifications-section [cv person]
  (when-let [certifications (seq (:person/certifications person))]
    {:kind :definitions
     :title "Sertifiseringer og kurs"
     :definitions
     (->> certifications
          (group-by :year)
          (sort-by (comp - first))
          (map render-certifications))}))

(defn education-section [cv person]
  (when-let [educations (seq (sort-by :list/idx (:person/education person)))]
    {:kind :definitions
     :title "Utdanning"
     :definitions
     (map (fn [{:keys [institution years subject]}]
            {:title (year-range years)
             :contents [[:h4.h6 [:em institution]]
                        [:p subject]]})
          educations)}))

(defn presentation-url [presentation]
  (or (:page/uri presentation)
      (:presentation/video-url presentation)
      (:presentation/slides-url presentation)
      (:presentation/source-url presentation)))

(defn render-presentations [[year presentations]]
  {:title (str year)
   :contents
   [[:ul.dotted.dotted-tight
     (map (fn [presentation]
            [:li
             (if-let [url (presentation-url presentation)]
               [:a.link {:href url} (:presentation/title presentation)]
               (:presentation/title presentation))
             (when-let [event (:presentation/event-name presentation)]
               (list " ("
                     (if-let [url (:presentation/event-url presentation)]
                       [:a {:href url} event]
                       event)
                     ")"))])
          presentations)]]})

(defn presentation-section [cv person]
  (when-let [presentations (seq (:person/presentations person))]
    {:kind :definitions
     :title "Presentasjoner"
     :definitions
     (->> presentations
          (sort-by :presentation/date)
          reverse
          (group-by (comp #(.getYear %) :presentation/date))
          (sort-by first)
          reverse
          (map render-presentations))}))

(defn open-source-projects [person]
  (concat
   (sort-by :list/idx (:person/open-source-projects person))
   (sort-by :list/idx (:person/open-source-contributions person))))

(defn proglang [project]
  (->> (h/unwrap-ident-list project :oss-project/tech-list)
       (filter #(= :proglang (:tech/type %)))
       first))

(defn oss-project [project]
  [:li.text.inline-text
   [:a {:href (:oss-project/url project)} (:oss-project/name project)]
   " - "
   (m/strip-paragraph (f/to-html (:oss-project/description project)))])

(defn oss-contrib [project]
  [:a {:href (:oss-project/url project)}
   (:oss-project/name project)])

(defn open-source-section [cv person]
  (when-let [projects (seq (open-source-projects person))]
    (let [db (d/entity-db cv)
          by-techs (group-by (comp :db/ident proglang) projects)]
      {:kind :definitions
       :title "Bidrag til fri programvare"
       :definitions
       (->> by-techs
            keys
            (person/prefer-techs (person/preferred-techs person))
            (map (fn [tech]
                   (let [projects (get by-techs tech)]
                     {:title (:tech/name (d/entity db tech))
                      :contents [[:ul.dotted.dotted-tight
                                  (map oss-project (filter :oss-project/description projects))
                                  (when-let [contribs (->> projects
                                                           (remove :oss-project/description)
                                                           seq)]
                                    [:li.text
                                     "Har bidratt til "
                                     (e/comma-separated
                                      (map oss-contrib contribs))])]]}))))})))

(defn one-of [m ks]
  (loop [[k & ks] ks]
    (if (nil? k)
      nil
      (or (get m k)
          (recur ks)))))

(defn side-project [project]
  {:title (one-of project [:cv/title :screencast/title :side-project/title :blog-post/title])
   :url (one-of project [:page/uri :screencast/url :side-project/url :blog-post/external-url])
   :summary (one-of project [:cv/description :cv/blurb :side-project/description :screencast/description :blog-post/blurb])})

(defn prefix-title [prefix]
  (fn [project]
    (update-in project [:title] #(str prefix %))))

(defn other-contributions [person]
  (concat
   (map side-project (sort-by :list/idx (:person/screencasts person)))
   (map side-project (sort-by :list/idx (:person/side-projects person)))
   (->> (:blog-post/_author person)
        (remove :blog-post/archived?)
        (sort-by :blog-post/published)
        reverse
        (map (comp (prefix-title "Artikkel: ") side-project)))))

(defn other-contributions-section [cv person]
  (when-let [contribs (seq (other-contributions person))]
    {:kind :definitions
     :title "Andre faglige bidrag"
     :definitions
     [{:contents
       (->> contribs
            (map (fn [{:keys [title url summary]}]
                   (e/teaser
                    {:url url
                     :title title
                     :content [:div.text (f/to-html summary)]}))))}]}))

(defn create-page [cv]
  (let [person (cv-profile cv)]
    {:sections
     (->> [{:kind :cv-intro
            :image (:person/cv-picture person)
            :friendly-name (:person/given-name person)
            :full-name (:person/full-name person)
            :title (:person/title person)
            :contact-lines [(:person/phone-number person)
                            (:person/email-address person)]
            :links (person/prep-presence-links (:person/presence person))
            :experience (format "Utvikler med %s erfaring" (years-of-experience person))
            :qualifications (:person/qualifications person)
            :quote (endorsement-highlight person)
            :description (f/to-html (:person/description person))
            :highlights (map project-highlight (:person/project-highlights person))}
           (technology-section cv person)
           (projects-section cv person)
           (endorsements-section cv person)
           (certifications-section cv person)
           (education-section cv person)
           (presentation-section cv person)
           (open-source-section cv person)
           (other-contributions-section cv person)
           {:kind :footer}]
          (remove nil?))}))


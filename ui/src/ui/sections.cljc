(ns ui.sections
  (:require [ui.elements :as e]
            [ui.layout :as l]
            [ui.icons :as icons]))

(defn article-section [{:keys [articles class] :as params}]
  [:div.section.article-section
   {:className class
    :style (l/stylish {} params)}
   [:div.content
    (map e/article articles)]])

(defn container-section [{:keys [content class] :as params}]
  [:div.section.container-section
   {:className class
    :style (l/stylish {} params)}
   [:div.content
    content]])

(defn banner-section [{:keys [logo text] :as params}]
  [:div.section
   {:style (l/stylish {} params)}
   [:div.content.tac.banner-ws
    (when logo
      [:img.banner-logo {:src logo}])
    (e/h1 {} text)]])

(defn bruce-section [{:keys [title text link image-front image-back] :as params}]
  [:div.section.bruce
   {:style (l/stylish {} params)}
   [:div.content
    [:div.gutter.gutter-l
     [:div.bruce-header (l/header)]
     [:div.bruce-content
      (e/h0 {} title)
      [:p.text text]
      (e/arrow-link link)]
     [:div.bruce-image-front
      [:img {:src image-front}]]
     [:div.bruce-image-back
      [:img {:src image-back}]]]]])

(defn contact-section [{:keys [email phone address contacts link] :as params}]
  [:div.section.bruce.contact-section
   {:style (l/stylish {} params)}
   [:div.content.mbxxl
    [:div.header-section (l/header)]
    [:div.contact-header.tac.mtxxl
     [:div email]
     [:div phone]
     [:div address]]
    [:div.contacts-1
     (for [contact contacts]
       [:div.mbl
        (e/round-media
         {:image (:image-round contact)
          :title (:name contact)
          :lines [(:title contact)
                  (:phone contact)
                  (:email contact)]})])
     (e/arrow-link link)]
    [:div.contacts-2
     (e/tango-grid (for [contact contacts]
                     {:content
                      (e/illustrated
                       {:curtain (:curtain contact)
                        :image (:image-tall contact)
                        :title (:name contact)
                        :lines [(:title contact)
                                (:phone contact)
                                (:email contact)]})}))
     (e/arrow-link link)]
    [:div.contact-map
     (e/google-map (:map params))]]])

(defn enumeration-section [{:keys [title categories] :as params}]
  [:div.section.enumeration-section {:style (l/stylish {} params)}
   [:div.content
    [:h3.h3 title]
    [:div.enum-cats
     (for [category categories]
       [:div.enum-cat
        [:div.enum-label (:label category)]
        [:div.enum-items
         (e/comma-separated
          (for [item (:items category)]
            (if (:href item)
              [:a.link {:href (:href item)} (:text item)]
              (:text item))))]])]]])

(defn grid-section-content [{:keys [items grid-type]}]
  [:div.content
   ((case grid-type
      :box-grid e/box-grid
      :card-grid e/card-grid) items)])

(defn grid-section [params]
  [:div.section.grid-section
   {:style (l/stylish {} params)}
   (grid-section-content params)])

(defn grid-header-section [params]
  [:div.section.grid-header-section
   {:style (l/stylish {} params)}
   [:div.content.header-section
    (l/header)]
   [:div.grid-section
    (grid-section-content params)]])

(defn hip-section [{:keys [title single left right] :as params}]
  [:div.section.hip-section {:style (l/stylish {} (assoc params :background :blanc))}
   [:div.content
    (when single
      [:div.hip-single-box
       [:div.hip-title [:h3.h3 title]]
       [:div.hip-label.mbs (:title single)]
       [:img.hip-single-img {:src (:image single)}]
       [:div (:content single)]])
    (when (and left right)
      [:div.hip-pair
       [:div
        [:div.hip-left-box
         [:div.hip-title [:h3.h3 title]]
         [:div.hip-label.mbs (:title left)]
         [:div (:content left)]
         [:img.hip-left-img {:src (:image left)}]]]
       [:div.hip-right-box
        [:div.hip-right-inner
         [:div.hip-right-img.w-style-img {:style {:background-image (str "url(" (:image right) ")")}}]
         [:div.hip-label.mbs (:title right)]
         [:div (:content right)]]]])]])

(defn profile-section [{:keys [full-name title mobile mail description image presence cv] :as params}]
  [:div.section {:style (l/stylish {} params)}
   [:div.content.header-section
    (l/header)]
   [:div.content.profile-section.mbxl
    [:div.profile-title
     [:h1.h1 full-name]
     [:h5.h5.mbs title]]
    [:div.profile-image [:img.img {:src image}]]
    (when cv
     [:div.profile-cv
      (e/arrow-link {:text (:text cv)
                     :href (:url cv)})])
    [:div.profile-contact
     [:div [:a {:href (str "tel:" mobile)} mobile]]
     [:div [:a {:href (str "mailto:" mail)} mail]]
     (e/icon-link-row {:links presence :class "mtm"})]
    [:div.profile-desc
     description]]])

(defn pønt-section [{:keys [portrait-1 portrait-2 top-triangle bottom-triangle top-circle bottom-circle] :as params}]
  [:div.section.pønt-section {:style (l/stylish {} params)}
   [:div.content
    [:div.gutter.gutter-xl
     [:div.pønt-item.portrait-1
      [:a {:href (:href portrait-1) :title (:title portrait-1)}
       [:img.img.image-style-chocolate-triangle {:src (:img portrait-1)}]]]

     [:div.pønt-item.top-triangle
      [:a {:href (:href top-triangle) :title (:title top-triangle)}
       [:img.img.image-style-rouge-triangle {:src (:img top-triangle)}]]]

     [:div.pønt-item.bottom-circle
      [:a {:href (:href bottom-circle) :title (:title bottom-circle)}
       [:img.img.image-style-chocolate-circle-pønt {:src (:img bottom-circle)}]]]

     [:div.pønt-item.portrait-2
      [:a {:href (:href portrait-2) :title (:title portrait-2)}
       [:img.img.image-style-rouge-triangle {:src (:img portrait-2)}]]]

     [:div.pønt-item.top-circle
      [:a {:href (:href top-circle) :title (:title top-circle)}
       [:img.img.image-style-rouge-circle-pønt {:src (:img top-circle)}]]]

     [:div.pønt-item.bottom-triangle
      [:a {:href (:href bottom-triangle) :title (:title bottom-triangle)}
       [:img.img.image-style-chocolate-triangle {:src (:img bottom-triangle)}]]]]]])

(defn seymour-section [{:keys [seymours] :as params}]
  [:div.section {:style (l/stylish {} params)}
   [:div.content.whitespaceorama
    [:div.trigrid
     (for [seymour seymours]
       [:div
        (e/seymour seymour)])]]])

(defn tech-intro-section [{:keys [title logo article] :as params}]
  [:div.section
   {:style (l/stylish {} params)}
   [:div.content.mbl
    [:div.tac.banner-ws
     (when logo
       [:img.banner-logo {:src logo}])
     (e/h1 {} title)]
    [:div.mtl
     (if (:aside article)
       (e/article article)
       (e/simple-article article))]]])

(defn titled-section [{:keys [title contents] :as params}]
  [:div.section.titled-section {:style (l/stylish {} params)}
   [:div.content
    [:div.titled-title
     [:h3.h3 title]]
    [:div.titled-content
     (interpose [:div.mbl] contents)]]])

(defmulti definition (fn [p] (:type p)))

(defmethod definition :separator [{:keys [title description category]}]
  [:div.definition.mbm
   (when category [:p.h6 category])
   [:h3.h4 title]
   (when description
     [:div.text.mts description])])

(defmethod definition :complex-title [{:keys [title contents]}]
  [:div.definition
   [:div.definition-title title]
   [:div.definition-content
    (seq contents)]])

(defmethod definition :default [params]
  (definition (-> params
                  (assoc :type :complex-title)
                  (assoc :title [:h4.h6 (or (:title params) " ")]))))

(defn definition-section [{:keys [definitions title id] :as params}]
  [:div.section.definition-section {:style (l/stylish {} params)
                                    :id id}
   [:div.content
    (if title
      (list [:h2.h3 title]
            [:hr.definition-separator-strong])
      [:hr.definition-separator])
    (->> definitions
         (map definition)
         (interpose [:hr.definition-separator]))
    [:hr.definition-separator]]])

(defn vertigo-section [{:keys [title text link image] :as params}]
  [:div.section.vertigo
   {:style (l/stylish {} params)}
   [:div.content
    [:div.gutter.gutter-l.grid
     {:style (l/add-pønt {} [{:kind :less-than
                              :position "right -300px top -410px"}])}
     [:div.vertigo-media
      [:div.inner-media
       [:img.img.image-style-vertigo {:src image}]]]
     [:div.vertigo-content
      [:div.inner-content
       (e/h2 {} title)
       [:p.text text]
       (e/arrow-link link)]]]]])

(defn widescreen-section [{:keys [image alt] :as params}]
  [:div.section.widescreen
   {:style (l/stylish {} params)}
   [:div.content
    [:div.content-l
     [:img.img {:src image :alt alt}]]]])

(defn cv-highlight [{:keys [title text href]}]
  [:div.cv-highlight
   [:h3.h4.mbs title]
   [:p text]
   (when href
     [:p.mts (e/arrow-link {:href href :text "Les mer"})])])

(defn cv-intro-section [{:keys [image friendly-name full-name title contact-lines links
                                experience qualifications quote description highlights]
                         :as params}]
  [:div.section.cv-section
   [:div.cv-wrapper
    {:style (->> (merge {:background :chablis
                         :pønt [{:kind :greater-than
                                 :position "left 0 top -280px"}]}
                        params)
                 (l/stylish {}))}
    [:div.content.header-section
     (l/header)]
    [:div.content.cv-content
     [:div.cv-image
      [:img
       {:src image
        :alt (str "Profilbilde av " full-name)}]]
     [:div.cv-essentials
      [:h1.h1 full-name]
      [:p.cv-title title]
      [:div.cv-details
       [:p.h6.cv-contact
        (interpose [:br] contact-lines)]
       (e/icon-link-row {:links links})]]]]
   [:div.content.cv-intro
    [:div.cv-intro-text
     [:div.mbxl
      [:h2.h4 experience]
      [:ul.dotted (for [q qualifications] [:li q])]]
     (when quote
       [:div.mbxxl
        [:div.mbm (e/blockquote {:quote (:text quote)})]
        (when-let [source (:source quote)]
          [:p [:cite "- " (:source quote)]])])
     [:h2.h3 "Om " friendly-name]
     [:div.text description]]
    [:div.cv-highlights
     (map cv-highlight highlights)]]])

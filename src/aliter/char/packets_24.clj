(ns aliter.char.packets-24
  (:use aliter.packets))


(defpackets
  [16r65 connect
   "Character server connect request."
   (:account-id  :int)
   (:id-a        :int)
   (:id-b        :int)
   (:client-type :short)
   (:gender      :byte)]

  [16r66 choose
   "Character selection request."
   (:num :byte)]

  [16r67 create
   "Character creation request."
   (:name       (:string 24))
   (:str        :byte)
   (:agi        :byte)
   (:vit        :byte)
   (:int        :byte)
   (:dex        :byte)
   (:luk        :byte)
   (:num        :byte)
   (:hair-color :short)
   (:hair-style :short)]

  [16r68 delete
   "Character deletion request."
   (:character-id :long)
   (:email        (:string 40))]

  [16r187 keepalive
   "Ping from client."
   (:account-id int)]

  [16r28d check-name
   "Check if a character can be renamed."
   (:account-id   :int)
   (:character-id :int)
   (:new-name     (:string 24))]

  [16r28f rename
   "Character rename request."
   (:character-id :int)]


  ; sent
  [16r6b characters
   "Send character list."
   (:length :short)
   (:max-slots :byte)
   (:available-slots :byte)
   (:premium-slots :byte)
   ([] (:byte 20))
   (:characters [(:id :int)
                 (:base-experience :int)
                 (:zeny :int)
                 (:job-experience :int)
                 (:job-level :int)
                 (0 :int) ; body state
                 (0 :int) ; health state
                 (:effects :int)
                 (:karma :int)
                 (:manner :int)
                 (:status-points :short)
                 (:hp :int)
                 (:max-hp :int)
                 (:sp :short)
                 (:max-sp :short)
                 (150 :short) ; walk speed
                 (:job :short)
                 (:hair-style :short)
                 (:view-weapon :short)
                 (:base-level :short)
                 (:skill-points :short)
                 (:view-head-bottom :short)
                 (:view-shield :short)
                 (:view-head-top :short)
                 (:view-head-middle :short)
                 (:hair-color :short)
                 (:clothes-color :short)
                 (:name (:string 24))
                 (:str :byte)
                 (:agi :byte)
                 (:vit :byte)
                 (:int :byte)
                 (:dex :byte)
                 (:luk :byte)
                 (:num :short)
                 (1 :short) ; 0 for renamed?
                 (:map (:string 16))
                 (0 :int) ; delete date
                 (0 :int) ; robe
                 ;(0 :int) ; change slot (0 = disabled)
                 ;(0 :int) ; unknown (0 = disabled)
                 ])]

  [16r6c refuse
   "Refuse connection."
   (:reason :byte)]

  [16r6d character-created
   "Character successfully created."
   (:id :int)
   (:base-experience :int)
   (:zeny :int)
   (:job-experience :int)
   (:job-level :int)
   (0 :int) ; body state
   (0 :int) ; health state
   (:effects :int)
   (:karma :int)
   (:manner :int)
   (:status-points :short)
   (:hp :int)
   (:max-hp :int)
   (:sp :short)
   (:max-sp :short)
   (150 :short) ; walk speed
   (:job :short)
   (:hair-style :short)
   (:view-weapon :short)
   (:base-level :short)
   (:skill-points :short)
   (:view-head-bottom :short)
   (:view-shield :short)
   (:view-head-top :short)
   (:view-head-middle :short)
   (:hair-color :short)
   (:clothes-color :short)
   (:name (:string 24))
   (:str :byte)
   (:agi :byte)
   (:vit :byte)
   (:int :byte)
   (:dex :byte)
   (:luk :byte)
   (:num :short)
   (1 :short)] ; 0 for renamed?

  [16r6e creation-failed
   "Character creation failed."
   (:reason :byte)]

  [16r6f character-deleted
   "Character deleted."]

  [16r70 deletion-failed
   "Character deletion failed."
   (:reason :byte)]

  [16r71 switch-to-zone
   "Switch to zone server after choosing character."
   (:character-id :int)
   (:map (:string 16))
   (:ip (:byte 4))
   (:port :short)]

  [16r28e name-check-result
   "Is a character renameable?"
   (:reason :short)]

  [16r290 rename-result
   "Rename request result."
   (:reason :short)]

  [16r889 pin-code
   "?"
   (0 :short)
   (0 :short)
   (:account-id :int)
   (0 :short)]
  )

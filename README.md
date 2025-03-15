# SimplePresentsPlus
## 概要
LGW向け、CrackShotに対応している簡単なプレゼントPLです!<br>
MythicMobs対応できなかった :face_with_spiral_eyes:<br>
使えないのに存在する設定があるのでお読みください。
## コマンド
使用方法<br>
``/presents <サブコマンド>``<br>
### サブコマンド
``get`` 受け取り期間内のプレゼントを受け取ります。プレゼントは1度しか受け取れません。<br>
``set`` 管理者用プレゼント設定GUIを開きます。<br>
``list`` 今設定されているプレゼントを表示します。<br>
``adminresetplayer <player>`` 特定のプレイヤーの受け取り履歴を消去します。履歴を消去されたプレイヤーは再びプレゼントを受け取れるようになります。<br>
``reload`` present.ymlの設定を再読み込みします。<br>
``help`` コマンド使用のhelpを表示します。<br>
## presents.yml
```
presents:
  NewYearGift:
    items:
      - type: VANILLA
        material: DIAMOND
        amount: 1
    start: "2025-01-01"
    end: "2025-04-10"
    message: "新年のプレゼントを受け取りました！"
    Predictive_Conversion: true
    Passed_at_login: true
```
ゲーム内で``/presents set``を実行してプレゼントと名前を設定するとこんな感じのコードがymlに生成されます。<br>
上のコードを例とします。<br>
``NewYearGift`` - サーバー内で決めた名前がここに入ります。<br>
``start`` ``end`` - 最初は自動で設定されるのでymlで編集してください。<br>
``message`` - お好きな(ロマンチックな)言葉を入れてください。<br>
``Predictive_Conversion`` - ``/presents get``の後に表示される予測変換で表示されるかどうかを設定でき、隠しコードなどを作れる！予定でしたがまず予測変換が出てこないので使えない設定です :cry: <br>
``Passed_at_login`` - ログインしたときに自動で渡すかどうかの設定のはずでしたが動きません :sob: <br>

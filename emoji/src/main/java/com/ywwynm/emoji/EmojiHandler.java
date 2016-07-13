package com.ywwynm.emoji;

import android.content.Context;
import android.text.Spannable;

import java.util.HashMap;

/**
 * Created by ywwynm on 2016/7/13.
 * handle Emojis
 */
public class EmojiHandler {

    private EmojiHandler() { }

    private static final HashMap<String, Integer> sEmojisMap = new HashMap<>();

    static {
        sEmojisMap.put("[01]", R.drawable.baidu_1);
        sEmojisMap.put("[02]", R.drawable.baidu_2);
        sEmojisMap.put("[03]", R.drawable.baidu_3);
        sEmojisMap.put("[04]", R.drawable.baidu_4);
        sEmojisMap.put("[05]", R.drawable.baidu_5);
        sEmojisMap.put("[06]", R.drawable.baidu_6);
        sEmojisMap.put("[07]", R.drawable.baidu_7);
        sEmojisMap.put("[08]", R.drawable.baidu_8);
        sEmojisMap.put("[09]", R.drawable.baidu_9);
        sEmojisMap.put("[10]", R.drawable.baidu_10);
        sEmojisMap.put("[11]", R.drawable.baidu_11);
        sEmojisMap.put("[12]", R.drawable.baidu_12);
        sEmojisMap.put("[13]", R.drawable.baidu_13);
        sEmojisMap.put("[14]", R.drawable.baidu_14);
        sEmojisMap.put("[15]", R.drawable.baidu_15);
        sEmojisMap.put("[16]", R.drawable.baidu_16);
        sEmojisMap.put("[17]", R.drawable.baidu_17);
        sEmojisMap.put("[18]", R.drawable.baidu_18);
        sEmojisMap.put("[19]", R.drawable.baidu_19);
        sEmojisMap.put("[20]", R.drawable.baidu_20);
        sEmojisMap.put("[21]", R.drawable.baidu_21);
        sEmojisMap.put("[22]", R.drawable.baidu_22);
        sEmojisMap.put("[23]", R.drawable.baidu_23);
        sEmojisMap.put("[24]", R.drawable.baidu_24);
        sEmojisMap.put("[25]", R.drawable.baidu_25);
        sEmojisMap.put("[26]", R.drawable.baidu_26);
        sEmojisMap.put("[27]", R.drawable.baidu_27);
        sEmojisMap.put("[28]", R.drawable.baidu_28);
        sEmojisMap.put("[29]", R.drawable.baidu_29);
        sEmojisMap.put("[30]", R.drawable.baidu_30);
        sEmojisMap.put("[31]", R.drawable.baidu_31);
        sEmojisMap.put("[32]", R.drawable.baidu_32);
        sEmojisMap.put("[33]", R.drawable.baidu_33);
        sEmojisMap.put("[34]", R.drawable.baidu_34);
        sEmojisMap.put("[35]", R.drawable.baidu_35);

        sEmojisMap.put("[36]", R.drawable.qq_1);
        sEmojisMap.put("[37]", R.drawable.qq_2);
        sEmojisMap.put("[38]", R.drawable.qq_3);
        sEmojisMap.put("[39]", R.drawable.qq_4);
        sEmojisMap.put("[40]", R.drawable.qq_5);
        sEmojisMap.put("[41]", R.drawable.qq_6);
        sEmojisMap.put("[42]", R.drawable.qq_7);
        sEmojisMap.put("[43]", R.drawable.qq_8);
        sEmojisMap.put("[44]", R.drawable.qq_9);
        sEmojisMap.put("[45]", R.drawable.qq_10);
        sEmojisMap.put("[46]", R.drawable.qq_11);
        sEmojisMap.put("[47]", R.drawable.qq_12);
        sEmojisMap.put("[48]", R.drawable.qq_13);
        sEmojisMap.put("[49]", R.drawable.qq_14);
        sEmojisMap.put("[50]", R.drawable.qq_15);
        sEmojisMap.put("[51]", R.drawable.qq_16);
        sEmojisMap.put("[52]", R.drawable.qq_17);
        sEmojisMap.put("[53]", R.drawable.qq_18);
        sEmojisMap.put("[54]", R.drawable.qq_19);
        sEmojisMap.put("[55]", R.drawable.qq_20);
        sEmojisMap.put("[56]", R.drawable.qq_21);
        sEmojisMap.put("[57]", R.drawable.qq_22);
        sEmojisMap.put("[58]", R.drawable.qq_23);
        sEmojisMap.put("[59]", R.drawable.qq_24);
        sEmojisMap.put("[60]", R.drawable.qq_25);
        sEmojisMap.put("[61]", R.drawable.qq_26);
        sEmojisMap.put("[62]", R.drawable.qq_27);
        sEmojisMap.put("[63]", R.drawable.qq_28);
        sEmojisMap.put("[64]", R.drawable.qq_29);
        sEmojisMap.put("[65]", R.drawable.qq_30);
        sEmojisMap.put("[66]", R.drawable.qq_31);
        sEmojisMap.put("[67]", R.drawable.qq_32);
        sEmojisMap.put("[68]", R.drawable.qq_33);
        sEmojisMap.put("[69]", R.drawable.qq_34);
        sEmojisMap.put("[70]", R.drawable.qq_35);
        sEmojisMap.put("[71]", R.drawable.qq_36);
        sEmojisMap.put("[72]", R.drawable.qq_37);
        sEmojisMap.put("[73]", R.drawable.qq_38);
        sEmojisMap.put("[74]", R.drawable.qq_39);
        sEmojisMap.put("[75]", R.drawable.qq_40);
        sEmojisMap.put("[76]", R.drawable.qq_41);
        sEmojisMap.put("[77]", R.drawable.qq_42);
        sEmojisMap.put("[78]", R.drawable.qq_43);
        sEmojisMap.put("[79]", R.drawable.qq_44);
        sEmojisMap.put("[80]", R.drawable.qq_45);
        sEmojisMap.put("[81]", R.drawable.qq_46);

//        for (int i = 1; i <= 46; i++) {
//            System.out.println("sEmojisMap.put(\"[" + (i + 35) + "]\", R.drawable.qq_" + i + ");");
//        }
    }

    public static void addEmojis(
            Context context, Spannable text, int emojiSize, int emojiAlignment, int textSize) {
        addEmojis(context, text, emojiSize, emojiAlignment, textSize, 0, -1);
    }

    public static void addEmojis(
            Context context, Spannable spannable, int emojiSize, int emojiAlignment,
            int textSize, int index, int length) {
        int textLength = spannable.length();
        int textLengthToProcessMax = textLength - index;
        int textLengthToProcess = length < 0 || length >= textLengthToProcessMax ?
                textLength : (length + index);

        // remove spans throughout all text
        EmojiSpan[] oldSpans = spannable.getSpans(0, textLength, EmojiSpan.class);
        for (EmojiSpan oldSpan : oldSpans) {
            spannable.removeSpan(oldSpan);
        }

        String text = spannable.toString();
        int skip;
        for (int i = index; i < textLengthToProcess; i += skip) {
            Integer icon = 0;
            char c = spannable.charAt(i);
            if (c == '[') {
                int npr = text.indexOf(']', i + 1);
                if (npr != -1) {
                    int npl = text.indexOf('[', i + 1);
                    if (npr < npl || npl == -1) {
                        skip = npr - i + 1;
                        String s = text.substring(i, i + skip);
                        icon = sEmojisMap.get(s);
                    } else {
                        skip = 1;
                    }
                } else {
                    break;
                }
            } else {
                skip = 1;
            }

            if (icon != null && icon > 0) {
                EmojiSpan emojiSpan = new EmojiSpan(
                        context, icon, emojiSize, emojiAlignment, textSize);
                spannable.setSpan(emojiSpan,
                        i, i + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

}

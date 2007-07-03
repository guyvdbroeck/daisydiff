/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff.lcs.block;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

/**
 * A Comparator for diffing corresponding changes resulting from a line-based diff.
 * For example, if a line-based diff says "these lines were replaced by those lines",
 * then this comparator can be used to compare the content in those lines.
 *
 * <p>This comparator has also a little bit of special treatment for HTML/XML tags,
 * ie it tries to treat tags as single entities to be compared.
 */
public class BlockComparator implements IRangeComparator {
    private List tokens;

    /**
     * @param text should contain the lines of text concatenated with a "\n" in between
     *        them.
     */
    public BlockComparator(StringBuilder text) {
        this.tokens = splitLineTokens(text);
    }

    public int getRangeCount() {
        return tokens.size();
    }

    public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
        String thisToken = getToken(thisIndex);
        String otherToken = ((BlockComparator)other).getToken(otherIndex);

        // treating newlines and spaces the same gives a good effect
        if ((thisToken.equals(" ") && otherToken.equals("\n")) || (thisToken.equals("\n") && otherToken.equals(" ")))
            return true;

        return thisToken.equals(otherToken);
    }

    public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
        return false;
    }

    public String getToken(int i) {
        if (i < tokens.size())
            return (String)tokens.get(i);
        return "";
    }

    public String substring(int startToken) {
        return substring(startToken, tokens.size());
    }

    public String substring(int startToken, int endToken) {
        if (startToken == endToken) {
            return (String)tokens.get(startToken);
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = startToken; i < endToken; i++) {
                result.append((String)tokens.get(i));
            }
            return result.toString();
        }
    }

    public String[] substringSplitted(int startToken) {
        return substringSplitted(startToken, tokens.size());
    }

    /**
     * Returns the substring as an array of strings, each array entry
     * corresponding to one line. The newlines themselves are also
     * entries in the array.
     */
    public String[] substringSplitted(int startToken, int endToken) {
        if (startToken == endToken) {
            return new String[] { (String)tokens.get(startToken) };
        } else {
            int resultPos = -1;
            String[] result = null;
            StringBuilder resultBuffer = new StringBuilder();
            for (int i = startToken; i < endToken; i++) {
                String token = (String)tokens.get(i);
                if (token.equals("\n")) {
                    if (resultBuffer.length() > 0) {
                        result = grow(result, 2);
                        result[++resultPos] = resultBuffer.toString();
                        result[++resultPos] = "\n";
                        resultBuffer.setLength(0);
                    } else {
                        result = grow(result, 1);
                        result[++resultPos] = "\n";
                    }
                } else {
                    resultBuffer.append(token);
                }
            }
            if (resultBuffer.length() > 0) {
                result = grow(result, 1);
                result[++resultPos] = resultBuffer.toString();
            } else if (result == null) {
                result = new String[0];
            }
            return result;
        }
    }

    private String[] grow(String[] strings, int count) {
        if (strings == null) {
            return new String[count];
        } else {
            String[] result = new String[strings.length + count];
            System.arraycopy(strings, 0, result, 0, strings.length);
            return result;
        }
    }

    private ArrayList splitLineTokens(StringBuilder text) {
        ArrayList tokens = new ArrayList(100);
        StringBuilder currentWord = new StringBuilder(100);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<': // begin of a HTML/XML tag: let it stick to the next word
                    if (currentWord.length() > 0) {
                        tokens.add(currentWord.toString());
                        currentWord.setLength(0);
                    }
                    currentWord.append(c);
                    break;
                case '/':
                    // special handling for (possible) closing HTML/XML tag
                    if (currentWord.length() == 1 && currentWord.charAt(0) == '<') {
                        currentWord.append(c);
                        break;
                    }
                    // else: no break so that code below gets executed
                case '>':
                    if (currentWord.length() > 2 && currentWord.charAt(0) == '<' && currentWord.charAt(1) == '/') {
                        currentWord.append(c);
                        break;
                    }
                case '.':
                case '!':
                case ',':
                case ';':
                case '?':
                case ' ':
                case '=':
                case '\'':
                case '"':
                case '\t':
                case '\r':
                case '\n':
                    if (currentWord.length() > 0) {
                        tokens.add(currentWord.toString());
                        currentWord.setLength(0);
                    }
                    tokens.add(String.valueOf(c));
                    break;
                default:
                    currentWord.append(c);
            }
        }

        if (currentWord.length() > 0) {
            tokens.add(currentWord.toString());
        }
        return tokens;
    }
}

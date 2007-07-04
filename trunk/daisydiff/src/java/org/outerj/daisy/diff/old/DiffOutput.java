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
package org.outerj.daisy.diff.lcs.classic;

/**
 * A DiffOutput instance must be provided to the {@link Diff} to handle the
 * result of the diff process.
 */
public interface DiffOutput {
    void startLine(DiffLineType type) throws Exception;

    void addUnchangedText(String text) throws Exception;

    void addChangedText(String text) throws Exception;

    void endLine() throws Exception;

    void skippedLines(int linesSkipped) throws Exception;
}

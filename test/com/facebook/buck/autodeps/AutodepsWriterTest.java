/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.autodeps;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.testutil.integration.DebuggableTemporaryFolder;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class AutodepsWriterTest {

  @Rule
  public DebuggableTemporaryFolder tmpFolder = new DebuggableTemporaryFolder();

  private ProjectWorkspace workspace;

  @Test
  public void testWriteAutodeps() throws IOException, InterruptedException, ExecutionException {
    workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "writer", tmpFolder);
    workspace.setUp();

    DepsForBuildFiles depsForBuildFiles = new DepsForBuildFiles();

    BuildTarget fooTarget = workspace.newBuildTarget("//java/com/example:foo");
    BuildTarget barTarget = workspace.newBuildTarget("//java/com/example:bar");
    BuildTarget bazTarget = workspace.newBuildTarget("//java/com/example/baz:baz");

    BuildTarget aTarget = workspace.newBuildTarget("//java/com/example:a");
    BuildTarget bTarget = workspace.newBuildTarget("//java/com/example:b");
    BuildTarget cTarget = workspace.newBuildTarget("//java/com/example:c");
    BuildTarget dTarget = workspace.newBuildTarget("//java/com/example:d");
    BuildTarget eTarget = workspace.newBuildTarget("//java/com/example:e");
    BuildTarget fTarget = workspace.newBuildTarget("//java/com/example:f");
    BuildTarget gTarget = workspace.newBuildTarget("//java/com/example:g");
    BuildTarget hTarget = workspace.newBuildTarget("//java/com/example:h");
    BuildTarget iTarget = workspace.newBuildTarget("//java/com/example:i");

    depsForBuildFiles.addDep(fooTarget, barTarget);
    depsForBuildFiles.addDep(fooTarget, bazTarget);

    depsForBuildFiles.addDep(barTarget, bazTarget);

    // Add in seemingly random order so we can verify that we don't get things back in alphabetical
    // order because we added things in alphabetical order.
    depsForBuildFiles.addDep(bazTarget, gTarget);
    depsForBuildFiles.addDep(bazTarget, hTarget);
    depsForBuildFiles.addDep(bazTarget, aTarget);
    depsForBuildFiles.addDep(bazTarget, eTarget);
    depsForBuildFiles.addDep(bazTarget, bTarget);
    depsForBuildFiles.addDep(bazTarget, iTarget);
    depsForBuildFiles.addDep(bazTarget, dTarget);
    depsForBuildFiles.addDep(bazTarget, fTarget);
    depsForBuildFiles.addDep(bazTarget, cTarget);

    int numWritten = AutodepsWriter.write(
        depsForBuildFiles,
        /* buildFileName */ "BUCK",
        new ObjectMapper(),
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()),
        /* numThreads */ 1);
    workspace.verify();
    assertEquals(2, numWritten);
  }
}

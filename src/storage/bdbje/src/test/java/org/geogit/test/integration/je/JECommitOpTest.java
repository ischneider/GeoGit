/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the LGPL 2.1 license, available at the root
 * application directory.
 */
package org.geogit.test.integration.je;

import java.io.File;

import org.geogit.api.Platform;
import org.geogit.api.TestPlatform;
import org.geogit.di.GeogitModule;
import org.geogit.storage.bdbje.JEStorageModule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class JECommitOpTest extends org.geogit.test.integration.CommitOpTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Override
    protected Injector createInjector() {
        File workingDirectory = tempFolder.newFolder("mockWorkingDir");
        Platform testPlatform = new TestPlatform(workingDirectory);
        return Guice.createInjector(Modules.override(new GeogitModule()).with(
                new JEStorageModule(), new TestModule(testPlatform)));
    }

}

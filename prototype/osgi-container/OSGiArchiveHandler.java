/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.extras.osgicontainer;

import org.glassfish.api.deployment.archive.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.osgiondemanddeployment.ManifestProcessor;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.sun.enterprise.util.io.FileUtils;

import javax.inject.Inject;

/**
 * Archive Handler for OSGi modules.
 *
 * @author Jerome Dochez
 */
@Service(name=OSGiArchiveDetector.OSGI_ARCHIVE_TYPE)
@Singleton
public class OSGiArchiveHandler extends GenericHandler implements CompositeHandler {

    @Inject
    private OSGiArchiveDetector detector;

    public String getArchiveType() {
        return OSGiArchiveDetector.OSGI_ARCHIVE_TYPE;
    }

    public boolean accept(ReadableArchive source, String entryName) {
        // we hide everything so far.
        return false;
    }

    public void initCompositeMetaData(DeploymentContext context) {
        // nothing to initialize
    }

    public boolean handles(ReadableArchive archive) throws IOException {
        return detector.handles(archive);
    }

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        return parent;
    }

    public String getDefaultApplicationName(ReadableArchive archive,
        DeploymentContext context) {
        return getDefaultApplicationNameFromArchiveName(archive);
    }
    
    /**
	 * Overriding the expand method of base class(GenericHandler) in order to
	 * support allowing wrapping of non-OSGi bundles when --type=osgi option is
	 * used in deploy command or GUI. Pl. see [GLASSFISH-16651]
	 * 
	 * @param source
	 *            of the expanding
	 * @param target
	 *            of the expanding
	 * @param context
	 *            deployment context
	 * @throws IOException
	 *             when the archive is corrupted
	 */
	@Override
	public void expand(ReadableArchive source, WritableArchive target,
			DeploymentContext context) throws IOException {
		
		Properties props = context
				.getCommandParameters(DeployCommandParameters.class).properties;
		
		if ((props != null) && (props.containsKey("ondemand"))) {
			String ondemandprop = props.getProperty("ondemand");
			if (Boolean.valueOf(ondemandprop)){
				//start ondemand deployment handing
				Enumeration<String> e = source.entries();
				while (e.hasMoreElements()) {
					String entryName = e.nextElement();
					InputStream bis = new BufferedInputStream(
							source.getEntry(entryName));
					OutputStream os = null;
					try {
						os = target.putNextEntry(entryName);
						FileUtils.copy(bis, os, source.getEntrySize(entryName));
					} finally {
						if (os != null) {
							target.closeEntry();
						}
						bis.close();
					}
				}
								
				//Add MANIFEST File To Target and Write the MANIFEST File To Target
				Manifest m = null;
				m = ManifestProcessor.processManifest(source.getURI().toURL());;
				if (m != null) {
					OutputStream os = null;
					try {
						os = target.putNextEntry(JarFile.MANIFEST_NAME);
						m.write(os);
					} finally {
						if (os != null) {
							target.closeEntry();
						}
					}
				}
			}
		}
		else{
			super.expand(source, target, context);
		}
	}
}

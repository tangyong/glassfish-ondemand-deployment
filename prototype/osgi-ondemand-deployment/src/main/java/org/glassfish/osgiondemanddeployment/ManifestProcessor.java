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

package org.glassfish.osgiondemanddeployment;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manifest Processor, in order to intercept bundle context for providing osgi service and starting required bundles.
 *
 * @author Tang Yong(tangyong@cn.fujitsu.com)
 */
public class ManifestProcessor {
	
	private static Logger logger =
            Logger.getLogger(ManifestProcessor.class.getPackage().getName());
	
	private static final String BUNDLE_ACTIVATOR_KEY = "Bundle-Activator";
	
	private static final String ONDEMAND_DEPLOYMENT_ACTIVATOR = "org.glassfish.osgiondemanddeployment.OndemandDeploymentActivator";
	
	private static final String IMPORT_PACKAGE_KEY = "Import-Package";
	
	private static final String PROXIED_BUNDLE_ACTIVATOR_KEY = "Proxied-Bundle-Activator";
	
	private static final String ONDEMAND_DEPLOYMENT_PACKAGE = "org.glassfish.osgiondemanddeployment";

	public static Manifest processManifest(URL url) throws IOException
    {
        final JarInputStream jis = new JarInputStream(url.openStream());
        try {
            Manifest oldManifest = jis.getManifest();
            Manifest newManifest = new Manifest(oldManifest);
            Attributes attrs = newManifest.getMainAttributes();
            
            String bundleActivator = attrs.getValue(BUNDLE_ACTIVATOR_KEY);
            
            attrs.putValue(BUNDLE_ACTIVATOR_KEY, ONDEMAND_DEPLOYMENT_ACTIVATOR);
            if (bundleActivator != null){
            	attrs.putValue(PROXIED_BUNDLE_ACTIVATOR_KEY, bundleActivator);
            }
            
            String importedPackage = attrs.getValue(IMPORT_PACKAGE_KEY);
            if (importedPackage == null){
            	attrs.putValue(IMPORT_PACKAGE_KEY, ONDEMAND_DEPLOYMENT_PACKAGE);
            }
            else{
            	attrs.putValue(IMPORT_PACKAGE_KEY, importedPackage + "," + ONDEMAND_DEPLOYMENT_PACKAGE);
            }

            logger.logp(Level.FINE, "ManifestProcessor", "processManifest", "New Attributes of the bundle = {0}", new Object[]{attrs});
            return newManifest;
        } finally {
            jis.close();
        }
    }
}

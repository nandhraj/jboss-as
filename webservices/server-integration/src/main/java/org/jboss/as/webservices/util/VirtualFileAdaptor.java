/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.webservices.util;

import static org.jboss.as.webservices.WSMessages.MESSAGES;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.util.URLLoaderAdapter;

/**
 * A VirtualFile adaptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Ales.Justin@jboss.org
 * @author alessio.soldano@jboss.com
 */
public final class VirtualFileAdaptor implements UnifiedVirtualFile {

    private static final long serialVersionUID = -4509594124653184349L;

    private transient VirtualFile file;

    public VirtualFileAdaptor(VirtualFile file) {
        this.file = file;
    }

    protected VirtualFile getFile() throws IOException {
        return file;
    }

    public UnifiedVirtualFile findChild(String child) throws IOException {
        final VirtualFile virtualFile = getFile();
        final VirtualFile childFile = file.getChild(child);
        if (!childFile.exists())
            throw MESSAGES.missingChild(child, virtualFile);
        return new VirtualFileAdaptor(childFile);
    }

    public URL toURL() {
        try {
            return getFile().toURL();
        } catch (Exception e) {
            return null;
        }
    }

    private Object writeReplace() {
        // TODO: hack to enable remote tests
        try {
            File archive = file.getPhysicalFile();
            if (archive.list().length == 0) {
                final File parent = file.getPhysicalFile().getParentFile();
                final File[] children = parent.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File fileOrDir) {
                        return fileOrDir.isFile();
                    }
                });
                archive = children[0];
            }
            // Offer different UnifiedVirtualFile implementation for deserialization process
            return new URLLoaderAdapter(archive.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<UnifiedVirtualFile> getChildren() throws IOException {
        List<VirtualFile> vfList = getFile().getChildren();
        if (vfList == null)
            return null;
        List<UnifiedVirtualFile> uvfList = new LinkedList<UnifiedVirtualFile>();
        for (VirtualFile vf : vfList) {
            uvfList.add(new VirtualFileAdaptor(vf));
        }
        return uvfList;
    }

    public String getName() {
        try {
            return getFile().getName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

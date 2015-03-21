package us.kbase.moduleservice.gwt.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.kbase.auth.AuthToken;
import us.kbase.jkidl.IncludeProvider;
import us.kbase.kidl.KbModule;
import us.kbase.kidl.KidlParseException;
import us.kbase.scripts.FileSaver;
import us.kbase.scripts.ModuleBuilder;
import us.kbase.scripts.NestedFileSaver;
import us.kbase.scripts.OneFileSaver;
import us.kbase.workspace.GetModuleInfoParams;
import us.kbase.workspace.ModuleInfo;
import us.kbase.workspace.WorkspaceClient;

public class GenerationServlet extends HttpServlet {
    private static final long serialVersionUID = -1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, final HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String token = request.getParameter("token");
            final String module = request.getParameter("module");
            String verText = request.getParameter("ver");
            String wsUrl = DeployConfig.getConfig().get("ws.url");
            URL wsURL = new URL(wsUrl);
            WorkspaceClient cl = token == null ? new WorkspaceClient(wsURL) : new WorkspaceClient(wsURL, new AuthToken(token));
            cl.setAuthAllowedForHttp(true);
            GetModuleInfoParams gmiParams = new GetModuleInfoParams().withMod(module);
            if (verText != null)
                gmiParams.setVer(Long.parseLong(verText));
            ModuleInfo mi = cl.getModuleInfo(gmiParams);
            String spec = mi.getSpec();
            response.setContentType("application/octet-stream");
            final String[] firstFile = {null, null};
            final ZipOutputStream[] zos = {null};
            FileSaver output = new FileSaver() {
                int fileCount = 0;
                @Override
                public Writer openWriter(String path) throws IOException {
                    //System.out.println("Open zip entry for path: " + path);
                    if (fileCount == 0 && !path.contains("/")) {
                        firstFile[0] = path;
                        return new StringWriter() {
                            @Override
                            public void close() throws IOException {
                                flush();
                                firstFile[1] = toString();
                                fileCount++;
                            }
                        };
                    } else {
                        flushFirstFileIntoZip();
                        final ZipOutputStream z = getZip();
                        z.putNextEntry(new ZipEntry(path));
                        return new OutputStreamWriter(z, Charset.forName("utf-8")) {
                            @Override
                            public void close() throws IOException {
                                flush();
                                z.closeEntry();
                                fileCount++;
                            }
                        };
                    }
                }
                @Override
                public OutputStream openStream(String path) throws IOException {
                    flushFirstFileIntoZip();
                    final ZipOutputStream z = getZip();
                    z.putNextEntry(new ZipEntry(path));
                    return new BufferedOutputStream(z) {
                        @Override
                        public void close() throws IOException {
                            flush();
                            z.closeEntry();
                            fileCount++;
                        }
                    };
                }
                @Override
                public File getAsFileOrNull(String path) throws IOException {
                    return null;
                }
                private ZipOutputStream getZip() throws IOException {
                    if (zos[0] == null) {
                        response.setHeader( "Content-Disposition", "attachment;filename=" + module + "_generated.zip");
                        final OutputStream ros = response.getOutputStream();
                        zos[0] = new ZipOutputStream(ros);
                    }
                    return zos[0];
                }
                private void flushFirstFileIntoZip() throws IOException {
                    if (firstFile[1] != null) {
                        if (fileCount != 1)
                            throw new IllegalStateException("Unexpected file count: " + fileCount);
                        if (zos[0] != null)
                            throw new IllegalStateException("Zip stream was already initialized");
                        final ZipOutputStream z = getZip();
                        z.putNextEntry(new ZipEntry(firstFile[0]));
                        z.write(firstFile[1].getBytes(Charset.forName("utf-8")));
                        z.closeEntry();
                        firstFile[0] = null;
                        firstFile[1] = null;
                    }
                }
            };

            Reader specFile = new StringReader(spec);
            String url = request.getParameter("url");
            boolean jsClientSide = bool(request.getParameter("js"));
            String jsClientName = request.getParameter("jsclname");
            boolean perlClientSide = bool(request.getParameter("perl"));
            String perlClientName = request.getParameter("perlclname");
            boolean perlServerSide = bool(request.getParameter("perlsrv"));
            String perlServerName = request.getParameter("perlsrvname");
            String perlImplName = request.getParameter("perlimplname");
            String perlPsgiName = request.getParameter("perlpsginame");
            boolean perlEnableRetries = bool(request.getParameter("perlenableretries"));
            boolean pyClientSide = bool(request.getParameter("py"));
            String pyClientName = request.getParameter("pyclname");
            boolean pyServerSide = bool(request.getParameter("pysrv"));
            String pyServerName = request.getParameter("pysrvname");
            String pyImplName = request.getParameter("pyimplname"); 
            boolean javaClientSide = bool(request.getParameter("java"));
            boolean javaServerSide = bool(request.getParameter("javasrv"));
            String javaPackageParent = param(request, "javapakage", "us.kbase");
            FileSaver javaSrcDir = new NestedFileSaver(output, param(request, "javasrc", "src"));
            FileSaver javaLibDir = request.getParameter("javalib") == null ? null :
                new NestedFileSaver(output, request.getParameter("javalib"));
            FileSaver javaBuildXml = request.getParameter("javabuildxml") == null ? null :
                new OneFileSaver(output, request.getParameter("javabuildxml"));
            String javaGwtPackage = request.getParameter("javagwt");
            boolean newStyle = true;
            IncludeProvider ip = new RemoteIncludeProvider(cl, mi);
            boolean jsonSchema = bool(request.getParameter("jsonschema"));
            FileSaver jsonSchemas = jsonSchema ? new NestedFileSaver(output, "jsonschema") : null;
            
            ModuleBuilder.generate(specFile, url, jsClientSide, jsClientName, 
                    perlClientSide, perlClientName, perlServerSide, perlServerName, 
                    perlImplName, perlPsgiName, perlEnableRetries, pyClientSide, 
                    pyClientName, pyServerSide, pyServerName, pyImplName, 
                    javaClientSide, javaServerSide, javaPackageParent, javaSrcDir, 
                    javaLibDir, javaBuildXml, javaGwtPackage, newStyle, ip, output, 
                    jsonSchemas);
            
            if (zos[0] == null) {
                if (firstFile[1] != null) {
                    response.setHeader( "Content-Disposition", "attachment;filename=" + firstFile[0]);
                    final OutputStream ros = response.getOutputStream();
                    OutputStreamWriter w = new OutputStreamWriter(ros, Charset.forName("utf-8"));
                    w.write(firstFile[1]);
                    w.close();
                }
            } else {
                if (firstFile[1] != null)
                    throw new IllegalStateException("Both in-memory file and zip stream were initialized");
                zos[0].close();
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    
    private static boolean bool(String val) {
        return val == null ? false : Boolean.parseBoolean(val);
    }

    private static String param(HttpServletRequest request, String name, String defVal) {
        String ret = request.getParameter(name);
        return ret == null ? defVal : ret;
    }
}

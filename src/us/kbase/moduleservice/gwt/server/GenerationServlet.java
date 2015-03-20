package us.kbase.moduleservice.gwt.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String token = request.getParameter("token");
            String module = request.getParameter("module");
            String wsUrl = DeployConfig.getConfig().get("ws.url");
            URL wsURL = new URL(wsUrl);
            WorkspaceClient cl = token == null ? new WorkspaceClient(wsURL) : new WorkspaceClient(wsURL, new AuthToken(token));
            ModuleInfo mi = cl.getModuleInfo(new GetModuleInfoParams().withMod(module));
            String spec = mi.getSpec();
            response.setContentType("application/octet-stream");
            response.setHeader( "Content-Disposition", "attachment;filename=generated.zip");
            OutputStream os = response.getOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(os);
            FileSaver output = new FileSaver() {
                @Override
                public Writer openWriter(String path) throws IOException {
                    System.out.println("Open zip entry for path: " + path);
                    zos.putNextEntry(new ZipEntry(path));
                    final OutputStreamWriter isw = new OutputStreamWriter(zos, Charset.forName("utf-8"));
                    return new Writer() {  // Unclosable writer wrapper
                        @Override
                        public void write(char[] cbuf) throws IOException {
                            isw.write(cbuf);
                        }
                        @Override
                        public void write(int c) throws IOException {
                            isw.write(c);
                        }
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            isw.write(cbuf, off, len);
                        }
                        @Override
                        public void write(String str) throws IOException {
                            isw.write(str);
                        }
                        @Override
                        public void write(String str, int off, int len)
                                throws IOException {
                            isw.write(str, off, len);
                        }
                        @Override
                        public void flush() throws IOException {
                            isw.flush();
                        }
                        @Override
                        public void close() throws IOException {
                            isw.flush();
                            zos.closeEntry();
                        }
                    };
                }
                @Override
                public OutputStream openStream(String path) throws IOException {
                    return null;
                }
                @Override
                public File getAsFileOrNull(String path) throws IOException {
                    return null;
                }
            };

            Reader specFile = new StringReader(spec);
            String url = null;
            boolean jsClientSide = true;
            String jsClientName = null;
            boolean perlClientSide = true;
            String perlClientName = null;
            boolean perlServerSide = false;
            String perlServerName = null;
            String perlImplName = null;
            String perlPsgiName = null;
            boolean perlEnableRetries = false;
            boolean pyClientSide = true;
            String pyClientName = null;
            boolean pyServerSide = false;
            String pyServerName = null;
            String pyImplName = null; 
            boolean javaClientSide = true;
            boolean javaServerSide = false;
            String javaPackageParent = "us.kbase";
            FileSaver javaSrcDir = new NestedFileSaver(output, "src");
            FileSaver javaLibDir = null;
            FileSaver javaBuildXml = null;
            String javaGwtPackage = null;
            boolean newStyle = true;
            IncludeProvider ip = new IncludeProvider() {
                @Override
                public Map<String, KbModule> parseInclude(String includeLine)
                        throws KidlParseException {
                    return null;
                }
            };
            FileSaver jsonSchemas = null;
            
            ModuleBuilder.generate(specFile, url, jsClientSide, jsClientName, 
                    perlClientSide, perlClientName, perlServerSide, perlServerName, 
                    perlImplName, perlPsgiName, perlEnableRetries, pyClientSide, 
                    pyClientName, pyServerSide, pyServerName, pyImplName, 
                    javaClientSide, javaServerSide, javaPackageParent, javaSrcDir, 
                    javaLibDir, javaBuildXml, javaGwtPackage, newStyle, ip, output, 
                    jsonSchemas);
            
            zos.close();
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}

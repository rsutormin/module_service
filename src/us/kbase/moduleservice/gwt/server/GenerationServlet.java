package us.kbase.moduleservice.gwt.server;

import java.io.File;
import java.io.IOException;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String token = request.getParameter("token");
            String module = request.getParameter("module");
            String verText = request.getParameter("ver");
            String wsUrl = DeployConfig.getConfig().get("ws.url");
            URL wsURL = new URL(wsUrl);
            WorkspaceClient cl = token == null ? new WorkspaceClient(wsURL) : new WorkspaceClient(wsURL, new AuthToken(token));
            GetModuleInfoParams gmiParams = new GetModuleInfoParams().withMod(module);
            if (verText != null)
                gmiParams.setVer(Long.parseLong(verText));
            ModuleInfo mi = cl.getModuleInfo(gmiParams);
            String spec = mi.getSpec();
            response.setContentType("application/octet-stream");
            response.setHeader( "Content-Disposition", "attachment;filename=" + module + "_generated.zip");
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
            IncludeProvider ip = new IncludeProvider() {
                @Override
                public Map<String, KbModule> parseInclude(String includeLine)
                        throws KidlParseException {
                    return null;
                }
            };
            FileSaver jsonSchemas = request.getParameter("jsonschema") == null ? null :
                new NestedFileSaver(output, request.getParameter("jsonschema"));
            
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
    
    private static boolean bool(String val) {
        return val == null ? false : Boolean.parseBoolean(val);
    }

    private static String param(HttpServletRequest request, String name, String defVal) {
        String ret = request.getParameter(name);
        return ret == null ? defVal : ret;
    }
}

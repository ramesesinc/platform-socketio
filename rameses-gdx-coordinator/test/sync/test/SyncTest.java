/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sync.test;

import com.rameses.service.ScriptServiceContext;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class SyncTest extends TestCase {
    
    public SyncTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSync() {
        Map env = new HashMap();
        env.put("app.debu", true);
        env.put("app.host", "localhost:8571");
        env.put("app.context", "gdx");
        env.put("app.cluster", "osiris3");
        ScriptServiceContext ctx = new ScriptServiceContext(env);
        TestSyncService svc = ctx.create("TestSyncService", env, TestSyncService.class);
        svc.sync();
    }
    
    interface TestSyncService {
        void sync();
    }
}

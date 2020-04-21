package rundeck
/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
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


import grails.testing.gorm.DomainUnitTest
import org.grails.orm.hibernate.HibernateDatastore
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.transaction.PlatformTransactionManager
import rundeck.CommandExec

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/*
 * rundeck.CommandExecTests.java
 * 
 * User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 * Created: 5/14/12 11:31 AM
 * 
 */
class CommandExecTests {

    static HibernateDatastore hibernateDatastore

    PlatformTransactionManager transactionManager

    @BeforeClass
    static void setupGorm() {
        hibernateDatastore = new HibernateDatastore(CommandExec)
    }

    @AfterClass
    static void shutdownGorm() {
        hibernateDatastore.close()
    }

    @Before
    void setup() {
        transactionManager = hibernateDatastore.getTransactionManager()
    }

    @Test
    void testAdhocRemoteStringToMap(){
        CommandExec t=new CommandExec(adhocRemoteString:'test1')
        assertEquals([exec:'test1'],t.toMap())
    }

    @Test
    void testAdhocRemoteStringNoArgsToMap(){
        CommandExec t=new CommandExec(adhocRemoteString:'test1',argString: 'blah')
        assertEquals([exec:'test1'],t.toMap())
    }

    @Test
    void testAdhocLocalStringToMap(){
        CommandExec t=new CommandExec(adhocLocalString:'test2')
        assertEquals([script:'test2'],t.toMap())
    }

    @Test
    void testAdhocLocalStringWithArgsToMap(){
        CommandExec t=new CommandExec(adhocLocalString:'test2',argString: 'test args')
        assertEquals([script:'test2',args:'test args'],t.toMap())
    }

    @Test
    void testAdhocFileStringToMap(){
        CommandExec t=new CommandExec(adhocFilepath:'test3')
        assertEquals([scriptfile:'test3'],t.toMap())
    }

    @Test
    void testAdhocFileStringWithArgsToMap(){
        CommandExec t = new CommandExec(adhocFilepath: 'test3',argString: 'test args3')
        assertEquals([scriptfile: 'test3',args:'test args3'], t.toMap())
    }

    @Test
    void testErrorHandlerExecToMap(){
        CommandExec h=new CommandExec(adhocRemoteString: 'testerr')
        CommandExec t=new CommandExec(adhocFilepath:'test3',errorHandler: h)
        assertEquals([scriptfile:'test3',errorhandler:[exec: 'testerr']],t.toMap())
    }

    @Test
    void testErrorHandlerScriptToMap(){
        CommandExec h = new CommandExec(adhocLocalString: 'testerr',argString: 'err args')
        CommandExec t = new CommandExec(adhocFilepath: 'test3', errorHandler: h)
        assertEquals([scriptfile: 'test3', errorhandler: [script: 'testerr',args: 'err args']], t.toMap())
    }

    @Test
    void testFileExtensionToMap() {
        CommandExec t = new CommandExec(adhocLocalString: 'test1', fileExtension: '.ext')
        assertEquals([script: 'test1',fileExtension:'.ext'], t.toMap())
    }

    //test fromMap

    @Test
    void testExecFromMap(){
        CommandExec t=CommandExec.fromMap([exec: 'commandstring'])
        assertEquals('commandstring',t.adhocRemoteString)
        assertNull(t.argString)
        assertNull(t.errorHandler)

        CommandExec t2=CommandExec.fromMap([exec: 'commandstring',args: 'arg string'])
        assertEquals('commandstring',t2.adhocRemoteString)
        assertNull(t.argString)
        assertNull(t2.errorHandler)
    }

    @Test
    void testScriptFromMap(){
        CommandExec t=CommandExec.fromMap([script: 'scriptstring'])
        assertEquals('scriptstring',t.adhocLocalString)
        assertNull(t.argString)
        assertNull(t.errorHandler)

        CommandExec t2 = CommandExec.fromMap([script: 'scriptstring', args: 'arg string'])
        assertEquals('scriptstring', t2.adhocLocalString)
        assertEquals('arg string', t2.argString)
        assertNull(t2.errorHandler)
    }

    @Test
    void testScriptFileExtensionFromMap(){
        CommandExec t=CommandExec.fromMap([script: 'scriptstring',fileExtension: 'boogy'])
        assertEquals('scriptstring',t.adhocLocalString)
        assertEquals('boogy', t.fileExtension)
        assertNull(t.argString)
        assertNull(t.errorHandler)
    }

    @Test
    void testFileFromMap(){
        CommandExec t=CommandExec.fromMap([scriptfile: 'scriptfile'])
        assertEquals('scriptfile',t.adhocFilepath)
        assertNull(t.argString)
        assertNull(t.errorHandler)

        CommandExec t2 = CommandExec.fromMap([scriptfile: 'scriptfile', args: 'arg string'])
        assertEquals('scriptfile', t.adhocFilepath)
        assertEquals('arg string', t2.argString)
        assertNull(t2.errorHandler)
    }

    //test createClone

    @Test
    void testCreateCloneExec(){
        CommandExec t = new CommandExec(adhocRemoteString: 'test1')
        CommandExec t1=t.createClone()
        assertEquals('test1',t1.adhocRemoteString)
        assertNull(t1.argString)
    }

    @Test
    void testCreateCloneExecArgs(){
        CommandExec t = new CommandExec(adhocRemoteString: 'test1',argString: 'arg string')
        CommandExec t1=t.createClone()
        assertEquals('test1',t1.adhocRemoteString)
        assertEquals('arg string',t1.argString)
    }

    @Test
    void testCreateCloneScript() {
        CommandExec t = new CommandExec(adhocLocalString: 'test1')
        CommandExec t1 = t.createClone()
        assertEquals('test1', t1.adhocLocalString)
        assertNull(t1.argString)
    }

    @Test
    void testCreateCloneScriptFileExtension() {
        CommandExec t = new CommandExec(adhocLocalString: 'test1',fileExtension: 'ext')
        CommandExec t1 = t.createClone()
        assertEquals('test1', t1.adhocLocalString)
        assertEquals('ext', t1.fileExtension)
        assertNull(t1.argString)
    }

    @Test
    void testCreateCloneFile() {
        CommandExec t = new CommandExec(adhocFilepath: 'test1')
        CommandExec t1 = t.createClone()
        assertEquals('test1', t1.adhocFilepath)
        assertNull(t1.argString)
    }

    @Test
    void testCreateCloneNoHandler() {
        CommandExec h = new CommandExec(adhocRemoteString: 'testerr')
        CommandExec t = new CommandExec(adhocFilepath: 'test1',errorHandler: h)
        CommandExec t1 = t.createClone()
        assertEquals('test1', t1.adhocFilepath)
        assertNull(t1.errorHandler)
    }

    @Test
    void testCreateCloneKeepgoing() {
        CommandExec h = new CommandExec(adhocRemoteString: 'testerr',keepgoingOnSuccess: true)
        CommandExec t1 = h.createClone()
        assertEquals('testerr', t1.adhocRemoteString)
        assertEquals(true, !!t1.keepgoingOnSuccess)
        assertNull(t1.errorHandler)
    }

    @Test
    void testCreateCloneKeepgoingFalse() {
        CommandExec h = new CommandExec(adhocRemoteString: 'testerr',keepgoingOnSuccess: false)
        CommandExec t1 = h.createClone()
        assertEquals('testerr', t1.adhocRemoteString)
        assertEquals(true, !t1.keepgoingOnSuccess)
        assertNull(t1.errorHandler)
    }
}

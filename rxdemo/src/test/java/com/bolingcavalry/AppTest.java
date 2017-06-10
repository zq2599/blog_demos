package com.bolingcavalry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /*
    public void testdoExecute(){
        App app = new App();
        app.doExecute();
        assertTrue( true );
    }

    public void testdoAction(){
        App app = new App();
        app.doAction();
        assertTrue( true );
    }

    public void testdoFromChain(){
        App app = new App();
        app.doFromChain();
        assertTrue( true );
    }

    public void testdoJustChain(){
        App app = new App();
        app.doJustChain();
        assertTrue( true );
    }

    public void testdoMap(){
        App app = new App();
        app.doMap();
        assertTrue( true );
    }

    public void testdoFlatMap(){
        App app = new App();
        app.doFlatMap();
        assertTrue( true );
    }
    */

    public void testdoSchedule(){
        App app = new App();
        app.doSchedule();
        assertTrue( true );
    }
}

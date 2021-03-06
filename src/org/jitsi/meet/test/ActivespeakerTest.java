/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * The tests for active speaker detection feature.
 *
 * @author Pawel Domas
 */
public class ActiveSpeakerTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public ActiveSpeakerTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(
            new ActiveSpeakerTest("testActiveSpeaker"));

        return suite;
    }

    /**
     * Active speaker test scenario.
     */
    public void testActiveSpeaker()
    {
        // Start 3rd peer
        setupThirdParticipant();

        // Mute all
        muteAllParticipants();

        // Owner becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(
            ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant());

        // 3rd peer becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(
            ConferenceFixture.getThirdParticipant(),
            ConferenceFixture.getSecondParticipant());

        // 2nd peer becomes active speaker - check from owner's perspective
        testActiveSpeaker(
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner());

        // Dispose 3rd
        disposeThirdParticipant();

        // Unmuted owner and the 2nd
        unMuteOwnerAndSecond();
    }

    private void setupThirdParticipant()
    {
        new SetupConference("startThirdParticipant")
            .startThirdParticipant();

        new SetupConference("checkThirdParticipantJoinRoom")
            .checkThirdParticipantJoinRoom();

        new SetupConference("waitsThirdParticipantToJoinConference")
            .waitsThirdParticipantToJoinConference();

        new SetupConference("waitForThirdParticipantSendReceiveData")
            .waitForThirdParticipantSendReceiveData();
    }

    private void muteAllParticipants()
    {
        new MuteTest("muteOwnerAndCheck").muteOwnerAndCheck();

        new MuteTest("muteParticipantAndCheck").muteParticipantAndCheck();

        new MuteTest("muteThirdParticipantAndCheck")
                .muteThirdParticipantAndCheck();
    }

    private void unMuteOwnerAndSecond()
    {
        new MuteTest("unMuteOwnerAndCheck").unMuteOwnerAndCheck();

        new MuteTest("unMuteParticipantAndCheck").unMuteParticipantAndCheck();
    }

    private void disposeThirdParticipant()
    {
        new DisposeConference("disposeThirdParticipant")
            .disposeThirdParticipant();
    }

    /**
     * Tries to make given participant an active speaker by un-muting him.
     * Verifies from <tt>peer2</tt> perspective if he has been displayed on
     * the large video area. Mutes him back.
     *
     * @param activeSpeaker <tt>WebDriver</tt> instance of the participant who
     *                      will be testes as an active speaker.
     * @param peer2 <tt>WebDriver</tt> of the participant who will be observing
     *              and verifying active speaker change.
     */
    private void testActiveSpeaker(WebDriver activeSpeaker, WebDriver peer2)
    {
        final String speakerEndpoint = MeetUtils.getResourceJid(activeSpeaker);

        // Unmute
        MeetUIUtils.clickOnToolbarButton(activeSpeaker, "mute");

        MeetUIUtils.verifyIsMutedStatus(
            speakerEndpoint, activeSpeaker, peer2, false);

        // Verify that the user is now an active speaker from peer2 perspective
        try
        {
            new WebDriverWait(peer2, 10)
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver d)
                    {
                        return speakerEndpoint.equals(
                            MeetUIUtils.getLargeVideoResource(d));
                    }
                });
        }
        catch (TimeoutException exc)
        {
            assertEquals(
                "Active speaker not displayed on large video",
                speakerEndpoint, MeetUIUtils.getLargeVideoResource(peer2));
        }

        // Mute back again
        MeetUIUtils.clickOnToolbarButton(activeSpeaker, "mute");

        MeetUIUtils.verifyIsMutedStatus(
            speakerEndpoint, activeSpeaker, peer2, true);
    }
}

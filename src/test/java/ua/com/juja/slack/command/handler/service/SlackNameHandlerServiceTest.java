package ua.com.juja.slack.command.handler.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.com.juja.slack.command.handler.UserBySlackName;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
public class SlackNameHandlerServiceTest {

    @Mock
    private UserBySlackName userBySlackName;
    @Captor
    ArgumentCaptor<List<String>> captor;

    private SlackNameHandlerService slackNameHandlerService;

    private UserData userFrom;
    private UserData user1;
    private UserData user2;
    private UserData user3;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        userFrom = new UserData("AAA000", "@slackFrom");
        user1 = new UserData("AAA111", "@slack1");
        user2 = new UserData("AAA222", "@slack2");
        user3 = new UserData("AAA333", "@slack3");
        slackNameHandlerService = new SlackNameHandlerService(userBySlackName);
    }

    @Test
    public void getSlackParsedCommandOneSlackInText() throws Exception {
        //given
        String text = "text " + user1.getSlack() + " TexT text.";
        List<UserData> responseFromUserService = Arrays.asList(userFrom, user1);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Collections.singletonList(user1));

        when(userBySlackName.findUsersBySlackNames(anyListOf(String.class))).thenReturn(responseFromUserService);

        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackNames(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("@slack1", "@slackFrom"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandThreeSlackInText() throws Exception {
        //given
        String text = "text " + user1.getSlack() + " TexT " + user2.getSlack() + " text. " + user3.getSlack();
        List<UserData> responseFromUserService = Arrays.asList(userFrom, user1, user2, user3);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2, user3));

        when(userBySlackName.findUsersBySlackNames(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackNames(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("@slack2", "@slack1", "@slackFrom", "@slack3"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandTwoSlackInText() throws Exception {
        //given
        String text = "text " + user1.getSlack() + " TexT " + user2.getSlack() + " text.";
        List<UserData> responseFromUserService = Arrays.asList(userFrom, user2, user1);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2));

        when(userBySlackName.findUsersBySlackNames(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackNames(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("@slack2", "@slack1", "@slackFrom"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandWithoutSlackInText() throws Exception {
        //given
        String text = "text without slack name TexT text.";
        List<UserData> responseFromUserService = Collections.singletonList(userFrom);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, new ArrayList<>());

        when(userBySlackName.findUsersBySlackNames(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        verify(userBySlackName).findUsersBySlackNames(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("@slackFrom"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldAddATToFromUserIfFromUserWithoutAT() {
        //given
        String text = "SomeText " + user1.getSlack();
        List<UserData> responseFromUserService = Arrays.asList(userFrom, user1);
        when(userBySlackName.findUsersBySlackNames(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand("slackFrom", text);
        //then
        assertEquals("@slackFrom", actual.getFromUser().getSlack());
    }
}
import json
import unittest

import create_test_apps as apps


class TestAppSetup(unittest.TestCase):
    def test_Sanity_test_apps_are_private_and_subscribe_to_nothing(self):
        for key in apps.APPS:
            m = apps.manifest(key)
            self.assertFalse(m["public"])
            self.assertEqual([], m["default_events"])
            self.assertFalse(m["hook_attributes"]["active"])
            self.assertTrue(m["name"].endswith(" Test"))

    def test_Sanity_permissions_match_the_public_apps_and_secrets_go_to_the_test_repository(self):
        self.assertEqual({"contents": "read", "metadata": "read", "pull_requests": "write"},
                         apps.manifest("publisher")["default_permissions"])
        self.assertEqual("write", apps.manifest("moderator")["default_permissions"]["contents"])
        self.assertEqual("robocode-dev/rumble-bots-test", apps.SECRET_REPOSITORY)

    def test_Sanity_page_carries_one_form_per_app_for_the_organization(self):
        text = apps.page("abc")
        self.assertEqual(2, text.count("<form "))
        self.assertIn("organizations/robocode-dev/settings/apps/new?state=abc", text)
        self.assertIn(json.dumps(apps.manifest("moderator")["name"]).strip('"'), text)


if __name__ == "__main__":
    unittest.main()

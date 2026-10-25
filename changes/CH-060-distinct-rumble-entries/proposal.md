---
id: CH-060-proposal
type: proposal
status: open
links: [CAP-014, C-002]
title: Distinct Rumble entries
---

# Distinct Rumble entries

This plan-less change introduces a participation rule allowing moderators to reject near-duplicates of existing catalog bots when their differences are primarily cosmetic and they make no substantial contribution to battle strategy. Licensed reuse remains welcome; similar behavior alone does not establish duplication. Authors must be able to explain their contribution and seek reconsideration from a moderator without a conflict of interest.

Update the author guide, moderator guide, catalog contract, submission design and changelog. Record the policy decision and a Human-class acceptance scenario. Existing automatic screening and merge behavior stay in place: this is a discretionary human rule for cases raised during review or through disputes, not a new automatic similarity detector or mandatory manual review of every submission. Repeated member identities in a TwinDuel team remain valid.

The main risk is discouraging legitimate reuse or independent implementations of common strategies. The alternative is to rely solely on owner slot limits, which do not establish whether an entry contributes anything distinct. The cheapest useful check is to compare the rule and moderator procedure against a renamed copy, a meaningful strategic derivative and an independently written bot with similar behavior. Revise the wording if any of these cases cannot be distinguished without a numerical similarity threshold or unsupported allegations of intent. Even formally correct guidance could fail contributors if moderators treat code overlap as automatic proof; require an evidence-based explanation and reconsideration.

The repository states no vision document. There is no software architecture, protocol, physics or Bot API change; the affected durable design is catalog eligibility and its human moderation procedure.
